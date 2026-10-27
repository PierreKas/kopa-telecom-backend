package com.kopa.customerselfcare.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** This service only publishes to {@code kopa.events} (transaction.completed,
 *  transaction.failed) - no queues/bindings here, those belong to the consumer
 *  (kopa.notifications). See architecture-reference.md s8.1. */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitConfig {

    public static final String ROUTING_KEY_TRANSACTION_COMPLETED = "transaction.completed";
    public static final String ROUTING_KEY_TRANSACTION_FAILED = "transaction.failed";

    @Bean
    public TopicExchange kopaEventsExchange(MessagingProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }
}
