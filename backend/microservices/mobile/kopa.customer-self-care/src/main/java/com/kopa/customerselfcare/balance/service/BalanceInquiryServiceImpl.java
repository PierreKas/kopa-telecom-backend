package com.kopa.customerselfcare.balance.service;

import com.kopa.customerselfcare.balance.client.MiddlewareBalanceClient;
import com.kopa.customerselfcare.balance.client.dto.MiddlewareBalanceRequest;
import com.kopa.customerselfcare.balance.client.dto.MiddlewareBalanceResponse;
import com.kopa.customerselfcare.balance.dto.BalanceDto;
import com.kopa.customerselfcare.balance.model.BalanceInquiryLog;
import com.kopa.customerselfcare.balance.repository.BalanceInquiryLogRepository;
import com.kopa.customerselfcare.common.CorrelationIdHolder;
import com.kopa.customerselfcare.common.KtrnMessageCodes;
import com.kopa.customerselfcare.common.exception.BadRequestException;
import com.kopa.customerselfcare.common.exception.NotFoundException;
import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class BalanceInquiryServiceImpl implements BalanceInquiryService {

    private static final String CURRENCY = "RWF";

    private final MiddlewareBalanceClient middlewareClient;
    private final BalanceInquiryLogRepository auditLog;

    public BalanceInquiryServiceImpl(MiddlewareBalanceClient middlewareClient,
                                     BalanceInquiryLogRepository auditLog) {
        this.middlewareClient = middlewareClient;
        this.auditLog = auditLog;
    }

    @Override
    public BalanceDto searchBalance(String msisdn, String ispCd) {
        Instant startedAt = Instant.now();

        MiddlewareBalanceResponse response;
        try {
            response = middlewareClient.checkBalance(new MiddlewareBalanceRequest(msisdn, ispCd, null));
        } catch (ServiceUnavailableException ex) {
            logInquiry(msisdn, ispCd, startedAt, false, null, null, null, ex.getMessage());
            throw ex;
        }

        boolean success = KtrnMessageCodes.SUCCESS.equals(response.msgcode());
        logInquiry(msisdn, ispCd, startedAt, success, response.msgcode(), response.message(),
                response.remainingAmount(), success ? null : response.message());

        if (!success) {
            throw mapKtrnError(response);
        }
        return new BalanceDto(msisdn, ispCd, response.remainingAmount(), CURRENCY, Instant.now());
    }

    private RuntimeException mapKtrnError(MiddlewareBalanceResponse response) {
        String code = response.msgcode();
        String message = response.message() != null
                ? response.message()
                : "KTRN balance lookup failed (msgcode " + code + ").";

        if (KtrnMessageCodes.MISSING_PARAMETER.equals(code) || KtrnMessageCodes.INVALID_VALUE.equals(code)) {
            return new BadRequestException(message);
        }
        if (KtrnMessageCodes.DATA_NOT_FOUND.equals(code)) {
            return new NotFoundException(message);
        }
        return new ServiceUnavailableException(message);
    }

    private void logInquiry(String msisdn, String ispCd, Instant startedAt, boolean success, String msgcode,
                            String msgcodeMessage, String remainingAmount, String errorDetail) {
        BalanceInquiryLog log = new BalanceInquiryLog();
        log.setId(UUID.randomUUID());
        log.setMsisdn(msisdn);
        log.setIspCd(ispCd);
        log.setRequestedAt(startedAt);
        log.setSuccess(success);
        log.setKtrnMsgcode(msgcode);
        log.setKtrnMessage(msgcodeMessage);
        log.setRemainingAmount(remainingAmount);
        log.setErrorDetail(errorDetail);
        log.setDurationMs(Instant.now().toEpochMilli() - startedAt.toEpochMilli());
        log.setCorrelationId(CorrelationIdHolder.current());
        auditLog.save(log);
    }
}
