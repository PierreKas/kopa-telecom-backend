package com.kopa.notifications.sms.service;

import com.kopa.notifications.sms.dto.DeliveryStatusRequest;
import com.kopa.notifications.sms.model.SmsDeliveryEvent;
import com.kopa.notifications.sms.model.SmsMessage;
import com.kopa.notifications.sms.model.SmsMessageStatus;
import com.kopa.notifications.sms.repository.SmsDeliveryEventRepository;
import com.kopa.notifications.sms.repository.SmsMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Handles a DLR forwarded by kopa.middleware-sms. InTouch's status can arrive as a sequence
 *  (P -> Q -> S -> D/U) - each hit appends one sms_delivery_events row and updates the terminal
 *  sms_messages.status with the latest known value. */
@Service
public class DeliveryStatusService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryStatusService.class);

    // http_intouch_api_1.2.pdf s3: P Processing, D Delivered, Q Queued, E Errored, S Sent, U Undelivered.
    private static final Map<String, SmsMessageStatus> STATUS_MAP = Map.of(
            "P", SmsMessageStatus.SENT,
            "Q", SmsMessageStatus.SENT,
            "S", SmsMessageStatus.SENT,
            "D", SmsMessageStatus.DELIVERED,
            "E", SmsMessageStatus.ERRORED,
            "U", SmsMessageStatus.UNDELIVERED
    );

    private final SmsMessageRepository smsMessageRepository;
    private final SmsDeliveryEventRepository smsDeliveryEventRepository;

    public DeliveryStatusService(SmsMessageRepository smsMessageRepository,
                                  SmsDeliveryEventRepository smsDeliveryEventRepository) {
        this.smsMessageRepository = smsMessageRepository;
        this.smsDeliveryEventRepository = smsDeliveryEventRepository;
    }

    @Transactional
    public void recordDeliveryStatus(DeliveryStatusRequest request) {
        Optional<SmsMessage> maybeMessage = smsMessageRepository.findByProviderMessageId(request.messageId());
        if (maybeMessage.isEmpty()) {
            logger.warn("Received DLR for unknown providerMessageId={}", request.messageId());
            return;
        }

        SmsMessage message = maybeMessage.get();

        SmsDeliveryEvent event = new SmsDeliveryEvent();
        event.setId(UUID.randomUUID().toString());
        event.setSmsMessageId(message.getId());
        event.setStatus(request.status());
        event.setRawPayload("messageid=" + request.messageId() + "&status=" + request.status());
        event.setReceivedAt(Instant.now());
        smsDeliveryEventRepository.save(event);

        SmsMessageStatus mapped = STATUS_MAP.get(request.status());
        if (mapped != null) {
            message.setStatus(mapped);
            message.setUpdatedAt(Instant.now());
            smsMessageRepository.save(message);
        }
    }
}
