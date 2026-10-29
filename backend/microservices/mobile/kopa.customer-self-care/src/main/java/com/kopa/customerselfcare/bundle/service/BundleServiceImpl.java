package com.kopa.customerselfcare.bundle.service;

import com.kopa.customerselfcare.bundle.client.MiddlewareBundleClient;
import com.kopa.customerselfcare.bundle.client.dto.BundleListRequest;
import com.kopa.customerselfcare.bundle.client.dto.BundleListResponse;
import com.kopa.customerselfcare.bundle.client.dto.BundlePurchaseCheckRequest;
import com.kopa.customerselfcare.bundle.client.dto.BundlePurchaseCheckResponse;
import com.kopa.customerselfcare.bundle.client.dto.BundlePurchaseRequest;
import com.kopa.customerselfcare.bundle.client.dto.BundlePurchaseResponse;
import com.kopa.customerselfcare.bundle.client.dto.EWalletCheckRequest;
import com.kopa.customerselfcare.bundle.client.dto.EWalletCheckResponse;
import com.kopa.customerselfcare.bundle.dto.BundleDto;
import com.kopa.customerselfcare.bundle.dto.BundleListResponseDto;
import com.kopa.customerselfcare.bundle.dto.PurchaseBundleRequest;
import com.kopa.customerselfcare.bundle.dto.PurchaseBundleResponse;
import com.kopa.customerselfcare.common.CorrelationIdHolder;
import com.kopa.customerselfcare.common.KtrnMessageCodes;
import com.kopa.customerselfcare.common.exception.BadRequestException;
import com.kopa.customerselfcare.common.exception.NotFoundException;
import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import com.kopa.customerselfcare.config.KopaBusinessProperties;
import com.kopa.customerselfcare.config.RabbitConfig;
import com.kopa.customerselfcare.events.EventPublisher;
import com.kopa.customerselfcare.events.TransactionCompletedEvent;
import com.kopa.customerselfcare.events.TransactionFailedEvent;
import com.kopa.customerselfcare.inquiry.model.InquiryType;
import com.kopa.customerselfcare.inquiry.model.ServiceInquiryLog;
import com.kopa.customerselfcare.inquiry.repository.ServiceInquiryLogRepository;
import com.kopa.customerselfcare.limit.service.TransactionLimitService;
import com.kopa.customerselfcare.transaction.model.Transaction;
import com.kopa.customerselfcare.transaction.model.TransactionStatus;
import com.kopa.customerselfcare.transaction.model.TransactionType;
import com.kopa.customerselfcare.transaction.repository.TransactionRepository;
import com.kopa.customerselfcare.transaction.service.TransactionFailureRecorder;
import com.kopa.customerselfcare.transaction.service.TransactionOtpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BundleServiceImpl implements BundleService {

    private final MiddlewareBundleClient middlewareClient;
    private final KopaBusinessProperties businessProperties;
    private final TransactionRepository transactions;
    private final TransactionLimitService limitService;
    private final ServiceInquiryLogRepository inquiryLogs;
    private final TransactionOtpService otpService;
    private final EventPublisher eventPublisher;
    private final TransactionFailureRecorder failureRecorder;

    public BundleServiceImpl(MiddlewareBundleClient middlewareClient, KopaBusinessProperties businessProperties,
                             TransactionRepository transactions, TransactionLimitService limitService,
                             ServiceInquiryLogRepository inquiryLogs, TransactionOtpService otpService,
                             EventPublisher eventPublisher, TransactionFailureRecorder failureRecorder) {
        this.middlewareClient = middlewareClient;
        this.businessProperties = businessProperties;
        this.transactions = transactions;
        this.limitService = limitService;
        this.inquiryLogs = inquiryLogs;
        this.otpService = otpService;
        this.eventPublisher = eventPublisher;
        this.failureRecorder = failureRecorder;
    }

    @Override
    public BundleListResponseDto listAvailable(String msisdn) {
        Instant startedAt = Instant.now();
        BundleListResponse response;
        try {
            response = middlewareClient.listAvailable(new BundleListRequest(msisdn, null));
        } catch (ServiceUnavailableException ex) {
            logInquiry(msisdn, startedAt, false, "MIDDLEWARE");
            throw ex;
        }

        boolean success = KtrnMessageCodes.SUCCESS.equals(response.msgcode());
        logInquiry(msisdn, startedAt, success, success ? null : "KTRN_BUSINESS");

        if (!success) {
            throw mapKtrnError(response.msgcode(), response.message());
        }

        List<BundleDto> bundles = response.items().stream()
                .map(i -> new BundleDto(i.productCode(), i.productName(), i.price()))
                .toList();
        return new BundleListResponseDto(msisdn, bundles);
    }

    private RuntimeException mapKtrnError(String code, String message) {
        String detail = message != null ? message : "KTRN bundle list failed (msgcode " + code + ").";
        if (KtrnMessageCodes.MISSING_PARAMETER.equals(code) || KtrnMessageCodes.INVALID_VALUE.equals(code)) {
            return new BadRequestException(detail);
        }
        if (KtrnMessageCodes.DATA_NOT_FOUND.equals(code)) {
            return new NotFoundException(detail);
        }
        return new ServiceUnavailableException(detail);
    }

    @Override
    @Transactional
    public PurchaseBundleResponse purchase(String msisdn, PurchaseBundleRequest request) {
        limitService.checkAndRecord(msisdn, TransactionType.BUNDLE_PURCHASE, request.price());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setMsisdn(msisdn);
        transaction.setTransactionType(TransactionType.BUNDLE_PURCHASE);
        transaction.setProductCode(request.productCode());
        transaction.setAmount(request.price());
        transaction.setStatus(TransactionStatus.INITIATED);
        transaction.setRequestedAt(Instant.now());
        transaction.setCorrelationId(CorrelationIdHolder.current());
        transactions.save(transaction);

        String soId = businessProperties.getSoId();

        try {
            EWalletCheckResponse eWallet = middlewareClient.checkEWallet(new EWalletCheckRequest(soId, request.productCode()));
            if (!eWallet.canPurchase()) {
                // Surface KTRN's real reason (e.g. "Can't get available offer by offerCode" for an
                // unrecognized PROD_CD) instead of always assuming it's a balance problem - the two
                // read very differently to a customer.
                String detail = eWallet.message() != null ? eWallet.message() : "This bundle can't be purchased right now.";
                throw fail(transaction, "KTRN_BUSINESS", detail);
            }

            BundlePurchaseCheckResponse check = middlewareClient.checkPurchase(
                    new BundlePurchaseCheckRequest(soId, request.productCode(), msisdn));
            if (!"Y".equalsIgnoreCase(check.result())) {
                String detail = check.message() != null ? check.message() : "KTRN declined this bundle purchase.";
                throw fail(transaction, "KTRN_BUSINESS", detail);
            }
        } catch (ServiceUnavailableException ex) {
            throw fail(transaction, "MIDDLEWARE", ex.getMessage());
        }

        transaction.setStatus(TransactionStatus.OTP_PENDING);
        transactions.save(transaction);

        otpService.requestOtp(transaction);

        return new PurchaseBundleResponse(transaction.getId(), transaction.getStatus().name(),
                "Verification code sent. Confirm with /api/v1/transactions/" + msisdn + "/" + transaction.getId() + "/otp/verify.");
    }

    @Override
    @Transactional
    public PurchaseBundleResponse completePurchase(Transaction transaction) {
        String soId = businessProperties.getSoId();
        try {
            BundlePurchaseResponse purchaseResult = middlewareClient.purchase(
                    new BundlePurchaseRequest(transaction.getMsisdn(), soId, transaction.getProductCode()));

            if (purchaseResult.result() == null || purchaseResult.result().isBlank()) {
                throw fail(transaction, "KTRN_BUSINESS", "KTRN did not confirm the bundle purchase.");
            }

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCompletedAt(Instant.now());
            transaction.setDurationMs(transaction.getCompletedAt().toEpochMilli() - transaction.getRequestedAt().toEpochMilli());
            transactions.save(transaction);
            publishOutcome(transaction, true);

            return new PurchaseBundleResponse(transaction.getId(), transaction.getStatus().name(), purchaseResult.result());
        } catch (ServiceUnavailableException ex) {
            throw fail(transaction, "MIDDLEWARE", ex.getMessage());
        }
    }

    /** Marks the transaction FAILED and returns (doesn't throw) a BadRequestException for the
     *  caller to throw - keeps the "this always ends the request" intent explicit at call sites.
     *  Persists via TransactionFailureRecorder (its own, independent database transaction) rather
     *  than inline - purchase()/completePurchase() are @Transactional, and the normal way they
     *  reject a request is to throw the exception this method returns, which makes Spring roll
     *  back everything done inside that method call, including an inline save of this very
     *  failure (confirmed live 2026-10-28 for the identical pattern in RechargeServiceImpl). */
    private BadRequestException fail(Transaction transaction, String failureLayer, String detail) {
        failureRecorder.recordFailure(transaction, failureLayer, detail);
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

    private void logInquiry(String msisdn, Instant startedAt, boolean success, String failureLayer) {
        ServiceInquiryLog log = new ServiceInquiryLog();
        log.setId(UUID.randomUUID().toString());
        log.setMsisdn(msisdn);
        log.setInquiryType(InquiryType.BUNDLE_LIST);
        log.setSuccess(success);
        log.setFailureLayer(failureLayer);
        log.setRequestedAt(startedAt);
        log.setDurationMs(Instant.now().toEpochMilli() - startedAt.toEpochMilli());
        log.setCorrelationId(CorrelationIdHolder.current());
        inquiryLogs.save(log);
    }
}
