package com.kopa.customerselfcare.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Read-only mirror of kopa.customer-identity's own {@code customers} table - same physical rows,
 * same database (kopa_db), different schema. Never written here; customer-identity owns this
 * data. See architecture-reference.md s2 rule 4 / s8.3 - this is the "every Mobile service
 * mirrors customer_identity.customers/.auth_tokens for token validation" pattern, implemented for
 * the first time in this service on 2026-10-28 (it previously didn't exist anywhere, leaving
 * every FR-05-FR-10 endpoint here completely unauthenticated).
 */
@Entity
@Table(name = "customers", schema = "customer_identity")
@Getter
@Setter
public class Customer {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 15)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }
}
