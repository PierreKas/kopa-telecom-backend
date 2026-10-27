package com.kopa.notifications.events;

/** Local copy of what customer-self-care publishes on routing key "transaction.failed". */
public record TransactionFailedEvent(
        String transactionId,
        String msisdn,
        String transactionType,
        String failureDetail,
        String correlationId
) {
}
