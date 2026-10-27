package com.kopa.notifications.events;

/** Local copy of what customer-self-care publishes on routing key "transaction.completed". */
public record TransactionCompletedEvent(
        String transactionId,
        String msisdn,
        String transactionType,
        String amount,
        String productCode,
        String correlationId
) {
}
