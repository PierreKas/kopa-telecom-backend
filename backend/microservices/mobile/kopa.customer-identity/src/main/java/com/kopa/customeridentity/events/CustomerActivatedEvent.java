package com.kopa.customeridentity.events;

/** Published on routing key "customer.activated" - kopa.notifications keeps its own local copy
 *  of this shape, never a shared dependency (architecture-reference.md s8.1). */
public record CustomerActivatedEvent(
        String customerId,
        String msisdn,
        String fullName,
        String correlationId
) {
}
