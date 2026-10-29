package com.kopa.customerselfcare.usage.service;

import com.kopa.customerselfcare.common.CorrelationIdHolder;
import com.kopa.customerselfcare.common.KtrnMessageCodes;
import com.kopa.customerselfcare.common.exception.BadRequestException;
import com.kopa.customerselfcare.common.exception.NotFoundException;
import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import com.kopa.customerselfcare.inquiry.model.InquiryType;
import com.kopa.customerselfcare.inquiry.model.ServiceInquiryLog;
import com.kopa.customerselfcare.inquiry.repository.ServiceInquiryLogRepository;
import com.kopa.customerselfcare.usage.client.MiddlewareUsageClient;
import com.kopa.customerselfcare.usage.client.dto.UsageSearchRequest;
import com.kopa.customerselfcare.usage.client.dto.UsageSearchResponse;
import com.kopa.customerselfcare.usage.dto.UsageItemDto;
import com.kopa.customerselfcare.usage.dto.UsageResponseDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UsageInquiryServiceImpl implements UsageInquiryService {

    private final MiddlewareUsageClient middlewareClient;
    private final ServiceInquiryLogRepository inquiryLogs;

    public UsageInquiryServiceImpl(MiddlewareUsageClient middlewareClient, ServiceInquiryLogRepository inquiryLogs) {
        this.middlewareClient = middlewareClient;
        this.inquiryLogs = inquiryLogs;
    }

    @Override
    public UsageResponseDto searchUsage(String msisdn) {
        Instant startedAt = Instant.now();
        UsageSearchResponse response;
        try {
            response = middlewareClient.searchUsage(new UsageSearchRequest(msisdn, null));
        } catch (ServiceUnavailableException ex) {
            logInquiry(msisdn, startedAt, false, "MIDDLEWARE");
            throw ex;
        }

        boolean success = KtrnMessageCodes.SUCCESS.equals(response.msgcode());
        logInquiry(msisdn, startedAt, success, success ? null : "KTRN_BUSINESS");

        if (!success) {
            throw mapKtrnError(response.msgcode(), response.message());
        }

        List<UsageItemDto> items = response.items().stream()
                .map(i -> new UsageItemDto(i.productCode(), i.productName(), i.flag(), i.allowance(), i.usage(), i.remaining()))
                .toList();
        return new UsageResponseDto(msisdn, items);
    }

    private RuntimeException mapKtrnError(String code, String message) {
        String detail = message != null ? message : "KTRN usage lookup failed (msgcode " + code + ").";
        if (KtrnMessageCodes.MISSING_PARAMETER.equals(code) || KtrnMessageCodes.INVALID_VALUE.equals(code)) {
            return new BadRequestException(detail);
        }
        if (KtrnMessageCodes.DATA_NOT_FOUND.equals(code)) {
            return new NotFoundException(detail);
        }
        return new ServiceUnavailableException(detail);
    }

    private void logInquiry(String msisdn, Instant startedAt, boolean success, String failureLayer) {
        ServiceInquiryLog log = new ServiceInquiryLog();
        log.setId(UUID.randomUUID().toString());
        log.setMsisdn(msisdn);
        log.setInquiryType(InquiryType.USAGE);
        log.setSuccess(success);
        log.setFailureLayer(failureLayer);
        log.setRequestedAt(startedAt);
        log.setDurationMs(Instant.now().toEpochMilli() - startedAt.toEpochMilli());
        log.setCorrelationId(CorrelationIdHolder.current());
        inquiryLogs.save(log);
    }
}
