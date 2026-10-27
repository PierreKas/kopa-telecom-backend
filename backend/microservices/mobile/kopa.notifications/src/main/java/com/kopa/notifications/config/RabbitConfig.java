package com.kopa.notifications.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * One topic exchange ({@code kopa.events}), one durable queue per event this service consumes,
 * each bound by its own routing key - architecture-reference.md s8.1. Each queue gets its own
 * listener with a local copy of the event shape (see events/ package); no typed Spring AMQP
 * binding, same reason as DigiSanté s7.1 - a typed binding resolves __TypeId__ against the
 * *publisher's* class, which doesn't exist on this service's classpath, and loops forever on
 * nack/redeliver.
 *
 * <p>Listener failures (e.g. the local DB or InTouch gateway being down, not a parse error -
 * those are dropped deliberately in the listener itself) go through {@link #rabbitListenerContainerFactory}:
 * up to 3 attempts with exponential backoff, then {@link RepublishMessageRecoverer} moves the
 * message to a {@code .dlq} queue on {@code kopa.events.dlx} instead of nacking it back onto the
 * live queue forever (there is no broker-level DLX/TTL ping-pong here, this is advice-chain-level
 * retry, so the live queue never hot-loops on a single poison/unavailable-dependency message).
 */
@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitConfig {

    public static final String ROUTING_KEY_CUSTOMER_ACTIVATED = "customer.activated";
    public static final String ROUTING_KEY_TRANSACTION_COMPLETED = "transaction.completed";
    public static final String ROUTING_KEY_TRANSACTION_FAILED = "transaction.failed";

    public static final String QUEUE_CUSTOMER_ACTIVATED = "notifications.customer.activated";
    public static final String QUEUE_TRANSACTION_COMPLETED = "notifications.transaction.completed";
    public static final String QUEUE_TRANSACTION_FAILED = "notifications.transaction.failed";

    private static final String DLX_EXCHANGE = "kopa.events.dlx";
    private static final String DLQ_ROUTING_KEY_PREFIX = "dlq.";

    @Bean
    public TopicExchange kopaEventsExchange(MessagingProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public TopicExchange kopaEventsDeadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue customerActivatedQueue() {
        return new Queue(QUEUE_CUSTOMER_ACTIVATED, true);
    }

    @Bean
    public Queue transactionCompletedQueue() {
        return new Queue(QUEUE_TRANSACTION_COMPLETED, true);
    }

    @Bean
    public Queue transactionFailedQueue() {
        return new Queue(QUEUE_TRANSACTION_FAILED, true);
    }

    @Bean
    public Queue customerActivatedDlq() {
        return new Queue(QUEUE_CUSTOMER_ACTIVATED + ".dlq", true);
    }

    @Bean
    public Queue transactionCompletedDlq() {
        return new Queue(QUEUE_TRANSACTION_COMPLETED + ".dlq", true);
    }

    @Bean
    public Queue transactionFailedDlq() {
        return new Queue(QUEUE_TRANSACTION_FAILED + ".dlq", true);
    }

    @Bean
    public Binding customerActivatedBinding(Queue customerActivatedQueue, TopicExchange kopaEventsExchange) {
        return BindingBuilder.bind(customerActivatedQueue).to(kopaEventsExchange).with(ROUTING_KEY_CUSTOMER_ACTIVATED);
    }

    @Bean
    public Binding transactionCompletedBinding(Queue transactionCompletedQueue, TopicExchange kopaEventsExchange) {
        return BindingBuilder.bind(transactionCompletedQueue).to(kopaEventsExchange).with(ROUTING_KEY_TRANSACTION_COMPLETED);
    }

    @Bean
    public Binding transactionFailedBinding(Queue transactionFailedQueue, TopicExchange kopaEventsExchange) {
        return BindingBuilder.bind(transactionFailedQueue).to(kopaEventsExchange).with(ROUTING_KEY_TRANSACTION_FAILED);
    }

    @Bean
    public Binding customerActivatedDlqBinding(Queue customerActivatedDlq, TopicExchange kopaEventsDeadLetterExchange) {
        return BindingBuilder.bind(customerActivatedDlq).to(kopaEventsDeadLetterExchange)
                .with(DLQ_ROUTING_KEY_PREFIX + ROUTING_KEY_CUSTOMER_ACTIVATED);
    }

    @Bean
    public Binding transactionCompletedDlqBinding(Queue transactionCompletedDlq, TopicExchange kopaEventsDeadLetterExchange) {
        return BindingBuilder.bind(transactionCompletedDlq).to(kopaEventsDeadLetterExchange)
                .with(DLQ_ROUTING_KEY_PREFIX + ROUTING_KEY_TRANSACTION_COMPLETED);
    }

    @Bean
    public Binding transactionFailedDlqBinding(Queue transactionFailedDlq, TopicExchange kopaEventsDeadLetterExchange) {
        return BindingBuilder.bind(transactionFailedDlq).to(kopaEventsDeadLetterExchange)
                .with(DLQ_ROUTING_KEY_PREFIX + ROUTING_KEY_TRANSACTION_FAILED);
    }

    // Republishes with routing key "dlq." + the message's original received routing key, so one
    // exchange + prefix covers all three queues above without a dedicated recoverer per listener.
    @Bean
    public RepublishMessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, DLX_EXCHANGE, DLQ_ROUTING_KEY_PREFIX);
    }

    @Bean
    public MethodInterceptor retryInterceptor(RepublishMessageRecoverer republishMessageRecoverer) {
        return RetryInterceptorBuilder.stateless()
                .maxRetries(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(republishMessageRecoverer)
                .build();
    }

    // Overrides Boot's auto-configured factory (same bean name) so every @RabbitListener in this
    // service picks up the retry/DLQ advice without each one naming a containerFactory.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MethodInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }
}
