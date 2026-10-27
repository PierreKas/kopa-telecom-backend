package com.kopa.customerselfcare.recharge.service;

import com.kopa.customerselfcare.common.CorrelationIdHolder;
import com.kopa.customerselfcare.common.exception.BadRequestException;
import com.kopa.customerselfcare.common.exception.NotFoundException;
import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import com.kopa.customerselfcare.config.RabbitConfig;
import com.kopa.customerselfcare.events.EventPublisher;
import com.kopa.customerselfcare.events.TransactionCompletedEvent;
import com.kopa.customerselfcare.events.TransactionFailedEvent;
import com.kopa.customerselfcare.limit.service.TransactionLimitService;
import com.kopa.customerselfcare.recharge.client.MiddlewareRechargeClient;
import com.kopa.customerselfcare.recharge.client.dto.RechargeCancelRequest;
import com.kopa.customerselfcare.recharge.client.dto.RechargeRequest;
import com.kopa.customerselfcare.recharge.client.dto.RechargeResponse;
import com.kopa.customerselfcare.recharge.client.dto.RechargeVoucherRequest;
import com.kopa.customerselfcare.recharge.dto.RechargeCancelRequestDto;
import com.kopa.customerselfcare.recharge.dto.RechargeRequestDto;
import com.kopa.customerselfcare.recharge.dto.RechargeResponseDto;
import com.kopa.customerselfcare.recharge.dto.RechargeVoucherRequestDto;
import com.kopa.customerselfcare.transaction.model.Transaction;
import com.kopa.customerselfcare.transaction.model.TransactionStatus;
import com.kopa.customerselfcare.transaction.model.TransactionType;
import com.kopa.customerselfcare.transaction.repository.TransactionRepository;
import com.kopa.customerselfcare.transaction.service.TransactionOtpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RechargeServiceImpl implements RechargeService {

    private static final String KTRN_SUCCESS_MSGCODE = "1001";

    private final MiddlewareRechargeClient middlewareClient;
    private final TransactionRepository transactions;
    private final TransactionLimitService limitService;
    private final TransactionOtpService otpService;
    private final EventPublisher eventPublisher;

    public RechargeServiceImpl(MiddlewareRechargeClient middlewareClient, TransactionRepository transactions,
                               TransactionLimitService limitService, TransactionOtpService otpService,
                               EventPublisher eventPublisher) {
        this.middlewareClient = middlewareClient;
        this.transactions = transactions;
        this.limitService = limitService;
        this.otpService = otpService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public RechargeResponseDto recharge(String msisdn, RechargeRequestDto request) {
        limitService.checkAndRecord(msisdn, TransactionType.RECHARGE, request.amount());

        Transaction transaction = startTransaction(msisdn, TransactionType.RECHARGE, null, request.amount());
        transaction.setNationalId(request.nationalId());
        transaction.setReqApprovalNo(request.approvalNo());
        transaction.setStatus(TransactionStatus.OTP_PENDING);
        transactions.save(transaction);

        otpService.requestOtp(transaction);

        return new RechargeResponseDto(transaction.getId(), transaction.getStatus().name(),
                "Verification code sent. Confirm with /api/v1/transactions/" + msisdn + "/" + transaction.getId() + "/otp/verify.");
    }

    @Override
    @Transactional
    public RechargeResponseDto completeRecharge(Transaction transaction) {
        try {
            RechargeResponse response = middlewareClient.recharge(new RechargeRequest(
                    transaction.getMsisdn(), transaction.getNationalId(), null,
                    transaction.getReqApprovalNo(), transaction.getAmount().toPlainString()));
            return complete(transaction, response);
        } catch (ServiceUnavailableException ex) {
            throw fail(transaction, "MIDDLEWARE", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public RechargeResponseDto rechargeByVoucher(String msisdn, RechargeVoucherRequestDto request) {
        limitService.checkAndRecord(msisdn, TransactionType.RECHARGE_VOUCHER, null);

        Transaction transaction = startTransaction(msisdn, TransactionType.RECHARGE_VOUCHER, null, null);

        try {
            RechargeResponse response = middlewareClient.rechargeByVoucher(
                    new RechargeVoucherRequest(msisdn, null, request.voucherNumber()));
            return complete(transaction, response);
        } catch (ServiceUnavailableException ex) {
            throw fail(transaction, "MIDDLEWARE", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public RechargeResponseDto cancel(String msisdn, String transactionId, RechargeCancelRequestDto request) {
        Transaction original = transactions.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("No such transaction."));
        if (!original.getMsisdn().equals(msisdn)) {
            throw new NotFoundException("No such transaction.");
        }
        if (original.getStatus() != TransactionStatus.SUCCESS) {
            throw new BadRequestException("Only a successful recharge can be cancelled.");
        }

        Transaction cancelTransaction = startTransaction(msisdn, TransactionType.RECHARGE_CANCEL,
                null, request.cancelAmount());

        try {
            RechargeResponse response = middlewareClient.cancel(new RechargeCancelRequest(
                    msisdn, request.nationalId(), null, original.getKtrnApprNo(),
                    request.cancelAmount().toPlainString()));
            return complete(cancelTransaction, response);
        } catch (ServiceUnavailableException ex) {
            throw fail(cancelTransaction, "MIDDLEWARE", ex.getMessage());
        }
    }

    private Transaction startTransaction(String msisdn, TransactionType type, String productCode, java.math.BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setMsisdn(msisdn);
        transaction.setTransactionType(type);
        transaction.setProductCode(productCode);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUBMITTED);
        transaction.setRequestedAt(Instant.now());
        transaction.setCorrelationId(CorrelationIdHolder.current());
        transactions.save(transaction);
        return transaction;
    }

    private RechargeResponseDto complete(Transaction transaction, RechargeResponse response) {
        boolean success = KTRN_SUCCESS_MSGCODE.equals(response.msgcode());
        transaction.setKtrnMsgcode(response.msgcode());
        transaction.setKtrnMessage(response.message());
        transaction.setStatus(success ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        if (!success) {
            transaction.setFailureLayer("KTRN_BUSINESS");
            transaction.setFailureDetail(response.message());
        }
        transaction.setCompletedAt(Instant.now());
        transaction.setDurationMs(transaction.getCompletedAt().toEpochMilli() - transaction.getRequestedAt().toEpochMilli());
        transactions.save(transaction);

        publishOutcome(transaction, success);

        if (!success) {
            throw new BadRequestException(response.message() != null ? response.message()
                    : "KTRN declined this request (msgcode " + response.msgcode() + ").");
        }
        return new RechargeResponseDto(transaction.getId(), transaction.getStatus().name(), response.message());
    }

    private BadRequestException fail(Transaction transaction, String failureLayer, String detail) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureLayer(failureLayer);
        transaction.setFailureDetail(detail);
        transaction.setCompletedAt(Instant.now());
        transaction.setDurationMs(transaction.getCompletedAt().toEpochMilli() - transaction.getRequestedAt().toEpochMilli());
        transactions.save(transaction);
        publishOutcome(transaction, false);
        return new BadRequestException(detail);
    }

    private void publishOutcome(Transaction transaction, boolean success) {
        String amount = transaction.getAmount() == null ? null : transaction.getAmount().toPlainString();
        if (success) {
            eventPublisher.publish(RabbitConfig.ROUTING_KEY_TRANSACTION_COMPLETED, new TransactionCompletedEvent(
                    transaction.getId(), transaction.getMsisdn(), transaction.getTransactionType().name(),
                    amount, transaction.getProductCode(), transaction.getCorrelationId()));
        } else {
            eventPublisher.publish(RabbitConfig.ROUTING_KEY_TRANSACTION_FAILED, new TransactionFailedEvent(
                    transaction.getId(), transaction.getMsisdn(), transaction.getTransactionType().name(),
                    transaction.getFailureDetail(), transaction.getCorrelationId()));
        }
    }
}
