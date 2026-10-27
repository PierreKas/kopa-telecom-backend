package com.kopa.customeridentity.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** This service only publishes to {@code kopa.events} (customer.activated,
 *  customer.registration-failed) - no queues/bindings here, those belong to the consumer
 *  (kopa.notifications). See architecture-reference.md s8.1. */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitConfig {

    public static final String ROUTING_KEY_CUSTOMER_ACTIVATED = "customer.activated";
    public static final String ROUTING_KEY_CUSTOMER_REGISTRATION_FAILED = "customer.registration-failed";

    @Bean
    public TopicExchange kopaEventsExchange(MessagingProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }
}
