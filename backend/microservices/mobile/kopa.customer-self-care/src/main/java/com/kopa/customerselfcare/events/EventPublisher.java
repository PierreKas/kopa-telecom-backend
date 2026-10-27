package com.kopa.customerselfcare.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopa.customerselfcare.config.MessagingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publishes raw JSON bytes directly - see kopa.customer-identity's EventPublisher javadoc for
 *  why this bypasses Spring AMQP's default message converter. */
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
            logger.error("Failed to publish event routingKey={}", routingKey, ex);
        }
    }
}
