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

/** Read-only mirror of kopa.customer-self-care's own {@code transactions} table - see
 *  {@link Customer}'s javadoc for why this mirror exists. Never written here. */
@Entity
@Table(name = "transactions", schema = "customer_self_care")
@Getter
@Setter
public class Transaction {

    @Id
    private String id;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "product_code")
    private String productCode;

    @Column
    private BigDecimal amount;

    @Column
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "ktrn_msgcode")
    private String ktrnMsgcode;

    @Column(name = "ktrn_message")
    private String ktrnMessage;

    @Column(name = "failure_layer")
    private String failureLayer;

    @Column(name = "failure_detail")
    private String failureDetail;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;
}
