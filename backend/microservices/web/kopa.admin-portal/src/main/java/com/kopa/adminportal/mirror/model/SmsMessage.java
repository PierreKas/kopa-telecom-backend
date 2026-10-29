package com.kopa.adminportal.mirror.model;

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

/** Read-only mirror of kopa.notifications's own {@code sms_messages} table - see
 *  {@link Customer}'s javadoc for why this mirror exists. Never written here. */
@Entity
@Table(name = "sms_messages", schema = "notifications")
@Getter
@Setter
public class SmsMessage {

    @Id
    private String id;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SmsPurpose purpose;

    @Column(name = "template_code", nullable = false)
    private String templateCode;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SmsMessageStatus status;

    @Column
    private BigDecimal cost;

    @Column(name = "failure_layer")
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
