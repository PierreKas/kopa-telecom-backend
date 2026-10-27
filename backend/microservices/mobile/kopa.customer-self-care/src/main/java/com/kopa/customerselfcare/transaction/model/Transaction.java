package com.kopa.customerselfcare.transaction.model;

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

/** Every money-moving operation (recharge, voucher recharge, cancel, bundle purchase) in one
 *  unified table - see db-diagram.md customer_self_care s2. productCode is nullable by design:
 *  a plain amount recharge has none, a bundle purchase does. */
@Entity
@Table(name = "transactions")
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

    @Column(length = 3)
    private String currency = "RWF";

    @Column(name = "ktrn_appr_no")
    private String ktrnApprNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.INITIATED;

    @Column(name = "ktrn_msgcode", length = 8)
    private String ktrnMsgcode;

    @Column(name = "ktrn_message", length = 128)
    private String ktrnMessage;

    @Column(name = "failure_layer", length = 20)
    private String failureLayer;

    @Column(name = "failure_detail")
    private String failureDetail;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "otp_code_hash")
    private String otpCodeHash;

    @Column(name = "otp_expires_at")
    private Instant otpExpiresAt;

    @Column(name = "otp_attempt_count", nullable = false)
    private int otpAttemptCount = 0;

    /** RECHARGE request's own field - nullable, only populated while OTP_PENDING for a recharge.
     *  Distinct from ktrnApprNo, which is KTRN's response approval number. */
    @Column(name = "national_id")
    private String nationalId;

    @Column(name = "req_approval_no")
    private String reqApprovalNo;

    public boolean isOtpExpired(Instant now) {
        return otpExpiresAt == null || otpExpiresAt.isBefore(now);
    }
}
