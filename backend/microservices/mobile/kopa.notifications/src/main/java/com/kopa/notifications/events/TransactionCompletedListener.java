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
public class TransactionCompletedListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCompletedListener.class);

    private final SmsDispatchService smsDispatchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionCompletedListener(SmsDispatchService smsDispatchService) {
        this.smsDispatchService = smsDispatchService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_TRANSACTION_COMPLETED)
    public void onTransactionCompleted(Message message) {
        TransactionCompletedEvent event;
        try {
            event = objectMapper.readValue(message.getBody(), TransactionCompletedEvent.class);
        } catch (Exception ex) {
            logger.error("Failed to parse transaction.completed event, dropping", ex);
            return;
        }

        boolean isBundlePurchase = event.productCode() != null && !event.productCode().isBlank();
        String rendered = isBundlePurchase
                ? SmsTemplates.purchaseConfirmation(event.productCode(), event.amount())
                : SmsTemplates.rechargeConfirmation(event.amount());
        String templateCode = isBundlePurchase ? SmsTemplates.PURCHASE_CONFIRMATION : SmsTemplates.RECHARGE_CONFIRMATION;
        SmsPurpose purpose = isBundlePurchase ? SmsPurpose.PURCHASE_CONFIRMATION : SmsPurpose.RECHARGE_CONFIRMATION;

        // See CustomerActivatedListener for why this is set manually here.
        try {
            CorrelationIdHolder.set(event.correlationId());
            smsDispatchService.dispatch(event.msisdn(), purpose, templateCode, rendered,
                    event.correlationId(), "customer-self-care");
        } finally {
            CorrelationIdHolder.clear();
        }
    }
}
