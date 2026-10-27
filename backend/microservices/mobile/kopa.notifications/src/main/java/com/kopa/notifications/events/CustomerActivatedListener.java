package com.kopa.notifications.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopa.notifications.common.CorrelationIdHolder;
import com.kopa.notifications.config.RabbitConfig;
import com.kopa.notifications.sms.model.SmsPurpose;
import com.kopa.notifications.sms.service.SmsDispatchService;
import com.kopa.notifications.sms.service.SmsTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerActivatedListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomerActivatedListener.class);

    private final SmsDispatchService smsDispatchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomerActivatedListener(SmsDispatchService smsDispatchService) {
        this.smsDispatchService = smsDispatchService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CUSTOMER_ACTIVATED)
    public void onCustomerActivated(Message message) {
        CustomerActivatedEvent event;
        try {
            event = objectMapper.readValue(message.getBody(), CustomerActivatedEvent.class);
        } catch (Exception ex) {
            logger.error("Failed to parse customer.activated event, dropping", ex);
            return;
        }

        // RabbitMQ consumer threads never go through CorrelationIdFilter (that's servlet-only) -
        // set it from the event payload ourselves so this listener's own logs, and the
        // sms_messages row it writes, trace back to the request that published the event.
        try {
            CorrelationIdHolder.set(event.correlationId());
            smsDispatchService.dispatch(event.msisdn(), SmsPurpose.ACTIVATION_CONFIRMATION,
                    SmsTemplates.ACTIVATION_CONFIRMATION, SmsTemplates.activationConfirmation(event.msisdn()),
                    event.correlationId(), "customer-identity");
        } finally {
            CorrelationIdHolder.clear();
        }
    }
}
