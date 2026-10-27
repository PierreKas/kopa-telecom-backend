package com.kopa.customeridentity.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopa.customeridentity.config.MessagingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes raw JSON bytes directly - deliberately bypasses Spring AMQP's default message
 * converter/__TypeId__ machinery (own ObjectMapper instance, same "don't rely on an injected
 * converter" pattern as KtrnApiClient's XmlMapper and IntouchSmsClient's ObjectMapper), so the
 * consumer side can deserialize with its own local copy of the event record without needing this
 * publisher's classes on its classpath - architecture-reference.md s8.1.
 */
@Component
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publish(String routingKey, Object payload) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(payload);
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            rabbitTemplate.send(properties.getExchange(), routingKey, new Message(body, messageProperties));
        } catch (Exception ex) {
            // Publishing is fire-and-forget by design (architecture-reference.md s5.2) - a broker
            // hiccup should never fail the HTTP response the client is waiting on.
            logger.error("Failed to publish event routingKey={}", routingKey, ex);
        }
    }
}
