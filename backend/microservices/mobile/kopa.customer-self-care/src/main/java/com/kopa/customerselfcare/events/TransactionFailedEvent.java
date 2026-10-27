package com.kopa.customerselfcare.events;

/** Published on routing key "transaction.failed". */
public record TransactionFailedEvent(
        String transactionId,
        String msisdn,
        String transactionType,
        String failureDetail,
        String correlationId
) {
}
