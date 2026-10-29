package com.kopa.customerselfcare.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Read-only mirror of kopa.customer-identity's own {@code auth_tokens} table - see
 * {@link Customer}'s javadoc for why this mirror exists. Never written here; customer-identity
 * is the only service that ever issues or revokes a token.
 */
@Entity
@Table(name = "auth_tokens", schema = "customer_identity")
@Getter
@Setter
public class AuthToken {

    @Id
    private String token;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    public boolean isUsable(Instant now) {
        return !revoked && expiresAt.isAfter(now);
    }
}
