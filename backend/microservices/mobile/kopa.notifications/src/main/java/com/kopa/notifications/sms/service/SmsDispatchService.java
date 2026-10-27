package com.kopa.notifications.sms.service;

import com.kopa.notifications.sms.client.MiddlewareSmsClient;
import com.kopa.notifications.sms.client.MiddlewareSmsResult;
import com.kopa.notifications.sms.model.SmsMessage;
import com.kopa.notifications.sms.model.SmsMessageStatus;
import com.kopa.notifications.sms.model.SmsPurpose;
import com.kopa.notifications.sms.repository.SmsMessageRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * "All sends - OTP or event-triggered - go through the same internal SmsDispatchService, which
 * calls kopa.middleware-sms's /internal/v1/sms/send, then writes one row to sms_messages" -
 * architecture-reference.md s5.3.
 */
@Service
public class SmsDispatchService {

    private final MiddlewareSmsClient middlewareSmsClient;
    private final SmsMessageRepository smsMessageRepository;

    public SmsDispatchService(MiddlewareSmsClient middlewareSmsClient, SmsMessageRepository smsMessageRepository) {
        this.middlewareSmsClient = middlewareSmsClient;
        this.smsMessageRepository = smsMessageRepository;
    }

    public SmsMessage dispatch(String msisdn, SmsPurpose purpose, String templateCode, String renderedMessage,
                                String correlationId, String requestedByService) {
        Instant now = Instant.now();

        SmsMessage record = new SmsMessage();
        record.setId(UUID.randomUUID().toString());
        record.setCorrelationId(correlationId);
        record.setMsisdn(msisdn);
        record.setPurpose(purpose);
        record.setTemplateCode(templateCode);
        record.setRenderedMessage(renderedMessage);
        record.setRequestedByService(requestedByService);
        record.setStatus(SmsMessageStatus.QUEUED);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        MiddlewareSmsResult result = middlewareSmsClient.send(msisdn, renderedMessage, null);

        if (result.success()) {
            record.setStatus(SmsMessageStatus.SENT);
            record.setProviderMessageId(result.messageId());
            record.setCost(result.cost() == null ? null : BigDecimal.valueOf(result.cost()));
        } else {
            record.setStatus(SmsMessageStatus.FAILED_TO_SEND);
            record.setFailureLayer("SMS_GATEWAY");
            record.setFailureDetail(result.errorMessage());
        }
        record.setUpdatedAt(Instant.now());

        return smsMessageRepository.save(record);
    }
}
