package com.kopa.customerselfcare.inquiry.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Read-only inquiries (usage, bundle browsing) - no money moved, so not a Transaction. Separate
 *  from balance_inquiry_log (feature 2.1.1) rather than merged into it, to avoid touching
 *  already-tested code; a future pass can unify them into one service_inquiry_log table per the
 *  original db-diagram.md plan if wanted. */
@Entity
@Table(name = "service_inquiry_log")
@Getter
@Setter
public class ServiceInquiryLog {

    @Id
    private String id;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_type", nullable = false)
    private InquiryType inquiryType;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_layer", length = 20)
    private String failureLayer;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "duration_ms")
    private Long durationMs;
}
