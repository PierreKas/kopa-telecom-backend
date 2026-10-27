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
public class TransactionFailedListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionFailedListener.class);

    private final SmsDispatchService smsDispatchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionFailedListener(SmsDispatchService smsDispatchService) {
        this.smsDispatchService = smsDispatchService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_TRANSACTION_FAILED)
    public void onTransactionFailed(Message message) {
        TransactionFailedEvent event;
        try {
            event = objectMapper.readValue(message.getBody(), TransactionFailedEvent.class);
        } catch (Exception ex) {
            logger.error("Failed to parse transaction.failed event, dropping", ex);
            return;
        }

        // See CustomerActivatedListener for why this is set manually here.
        try {
            CorrelationIdHolder.set(event.correlationId());
            smsDispatchService.dispatch(event.msisdn(), SmsPurpose.TRANSACTION_FAILED,
                    SmsTemplates.TRANSACTION_FAILED, SmsTemplates.transactionFailed(event.transactionType()),
                    event.correlationId(), "customer-self-care");
        } finally {
            CorrelationIdHolder.clear();
        }
    }
}
