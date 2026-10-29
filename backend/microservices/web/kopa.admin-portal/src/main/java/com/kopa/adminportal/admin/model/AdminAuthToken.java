package com.kopa.adminportal.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Opaque bearer token (SHA-256 hex), used purely as a lookup key - not a JWT. */
@Entity
@Table(name = "admin_auth_tokens")
@Getter
@Setter
public class AdminAuthToken {

    @Id
    private String token;

    @Column(name = "admin_id", nullable = false)
    private String adminId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    public boolean isUsable(Instant now) {
        return !revoked && expiresAt.isAfter(now);
    }
}
