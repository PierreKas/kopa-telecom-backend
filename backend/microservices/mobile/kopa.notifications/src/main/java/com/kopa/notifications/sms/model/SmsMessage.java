package com.kopa.notifications.sms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** "No domain data of its own, it's a send-log" - every SMS send (OTP or event-triggered) writes
 *  exactly one row here, via SmsDispatchService. See db-diagram.md notifications s3. */
@Entity
@Table(name = "sms_messages")
@Getter
@Setter
public class SmsMessage {

    @Id
    private String id;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SmsPurpose purpose;

    @Column(name = "template_code", nullable = false)
    private String templateCode;

    @Column(name = "rendered_message", nullable = false)
    private String renderedMessage;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SmsMessageStatus status = SmsMessageStatus.QUEUED;

    @Column
    private BigDecimal cost;

    @Column(name = "failure_layer", length = 20)
    private String failureLayer;

    @Column(name = "failure_detail")
    private String failureDetail;

    @Column(name = "requested_by_service", nullable = false)
    private String requestedByService;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
