package com.kopa.notifications.events;

/** Local copy of what customer-identity publishes on routing key "customer.activated" - never
 *  shared as a dependency, see RabbitConfig javadoc for why. */
public record CustomerActivatedEvent(
        String customerId,
        String msisdn,
        String fullName,
        String correlationId
) {
}
