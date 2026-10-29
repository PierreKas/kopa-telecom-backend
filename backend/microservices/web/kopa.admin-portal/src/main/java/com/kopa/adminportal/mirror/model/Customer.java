package com.kopa.adminportal.mirror.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Read-only mirror of kopa.customer-identity's own {@code customers} table - same physical rows,
 * same database (kopa_db), different schema. Never written here; customer-identity owns this
 * data. See architecture-reference.md s2 rule 4 / s6 / s8.3.
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

    @Column(name = "national_id", length = 20)
    private String nationalId;

    @Column(length = 19)
    private String iccid;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "birth_day")
    private LocalDate birthDay;

    @Column
    private String email;

    @Column(name = "so_id", length = 3)
    private String soId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
