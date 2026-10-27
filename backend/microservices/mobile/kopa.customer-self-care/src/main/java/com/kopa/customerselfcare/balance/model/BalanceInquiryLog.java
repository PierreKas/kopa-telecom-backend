package com.kopa.customerselfcare.balance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Audit trail of feature 2.1.1 (Searching Balance) calls - KTRN is the system of record for the
 *  balance itself, this table only logs who asked, when, and what came back. */
@Entity
@Table(name = "balance_inquiry_log")
@Getter
@Setter
public class BalanceInquiryLog {

    @Id
    private UUID id;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Column(name = "isp_cd", length = 3)
    private String ispCd;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "ktrn_msgcode", length = 5)
    private String ktrnMsgcode;

    @Column(name = "ktrn_message", length = 100)
    private String ktrnMessage;

    @Column(name = "remaining_amount", length = 15)
    private String remainingAmount;

    @Column(name = "error_detail", length = 255)
    private String errorDetail;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "correlation_id")
    private String correlationId;
}
