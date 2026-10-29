package com.kopa.notifications.sms.service;

import com.kopa.notifications.config.DevSmsProperties;
import com.kopa.notifications.sms.client.MiddlewareSmsClient;
import com.kopa.notifications.sms.client.MiddlewareSmsResult;
import com.kopa.notifications.sms.model.SmsMessage;
import com.kopa.notifications.sms.model.SmsMessageStatus;
import com.kopa.notifications.sms.model.SmsPurpose;
import com.kopa.notifications.sms.repository.SmsMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SmsDispatchService.class);

    private final MiddlewareSmsClient middlewareSmsClient;
    private final SmsMessageRepository smsMessageRepository;
    private final DevSmsProperties devSmsProperties;

    public SmsDispatchService(MiddlewareSmsClient middlewareSmsClient, SmsMessageRepository smsMessageRepository,
                               DevSmsProperties devSmsProperties) {
        this.middlewareSmsClient = middlewareSmsClient;
        this.smsMessageRepository = smsMessageRepository;
        this.devSmsProperties = devSmsProperties;
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

        // TEMPORARY dev override (see DevSmsProperties) - sms_messages still records the TRUE
        // intended recipient above; only the number actually dialled is redirected.
        String dialledMsisdn = msisdn;
        if (devSmsProperties.getDevRecipientOverride() != null && !devSmsProperties.getDevRecipientOverride().isBlank()) {
            dialledMsisdn = devSmsProperties.getDevRecipientOverride();
            logger.info("Dev SMS override active: redirecting SMS intended for {} to {}", msisdn, dialledMsisdn);
        }

        MiddlewareSmsResult result = middlewareSmsClient.send(dialledMsisdn, renderedMessage, null);

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
