package com.kopa.customerselfcare.events;

/** Published on routing key "transaction.completed" - kopa.notifications keeps its own local
 *  copy of this shape, never a shared dependency (architecture-reference.md s8.1). */
public record TransactionCompletedEvent(
        String transactionId,
        String msisdn,
        String transactionType,
        String amount,
        String productCode,
        String correlationId
) {
}
