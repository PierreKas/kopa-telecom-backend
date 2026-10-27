package com.kopa.customeridentity.registration.model;

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

/** Tracks the multi-step KTRN onboarding workflow (price plan search -> KYC verify -> simple
 *  activate -> subscribe customer) for one MSISDN. See db-diagram.md customer_identity schema. */
@Entity
@Table(name = "activation_requests")
@Getter
@Setter
public class ActivationRequest {

    @Id
    private String id;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Column(length = 19)
    private String iccid;

    @Column(name = "product_code")
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivationStatus status = ActivationStatus.PENDING;

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
}
