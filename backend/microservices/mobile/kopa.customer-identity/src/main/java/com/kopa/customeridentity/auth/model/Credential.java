package com.kopa.customeridentity.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** BCrypt hash of a customer's 6-digit PIN. Split from customers so a token-validation read
 *  never pulls the hash into memory - same convention as digi.sante.auth-access.Credential. */
@Entity
@Table(name = "credentials")
@Getter
@Setter
public class Credential {

    @Id
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }
}
