package com.kopa.customeridentity.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** PIN_RESET only for now - see db-diagram.md notes on why TRANSACTION_AUTH is handled locally
 *  on customer_self_care.transactions instead of here. */
@Entity
@Table(name = "otp_challenges")
@Getter
@Setter
public class OtpChallenge {

    @Id
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpPurpose purpose;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }
}
