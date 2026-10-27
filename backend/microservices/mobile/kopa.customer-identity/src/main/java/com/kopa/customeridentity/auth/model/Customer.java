package com.kopa.customeridentity.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** One row per subscriber - the table every other Mobile-side service mirrors read-only to
 *  resolve a bearer token (architecture-reference.md 2 rule 4 / 8.3). */
@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 15)
    private String msisdn;

    @Column(name = "national_id", length = 20)
    private String nationalId;

    @Column(name = "passport_number", length = 20)
    private String passportNumber;

    @Column(length = 19)
    private String iccid;

    @Column(name = "cust_id", length = 10)
    private String custId;

    @Column(name = "cust_type", length = 1)
    private String custType;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "birth_day")
    private LocalDate birthDay;

    @Column
    private String email;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "so_id", length = 3)
    private String soId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.PENDING_ACTIVATION;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }
}
