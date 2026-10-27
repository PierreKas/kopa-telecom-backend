package com.kopa.customeridentity.registration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** One row per KYC check attempted during onboarding (NIDA/RRA/DGIE). Deliberately stores only a
 *  reference to the input (national ID / TIN / passport number), never the raw photo/fingerprint
 *  payloads the KTRN calls carry - those are biometric data with no reason to persist locally.
 *  {@code activationRequestId} is nullable: a client verifies identity (verify-nida/rra/passport)
 *  before the /activate call that creates the ActivationRequest row exists, so this is keyed by
 *  {@code msisdn} in the meantime, not FK'd to one. */
@Entity
@Table(name = "kyc_verification_log")
@Getter
@Setter
public class KycVerificationLog {

    @Id
    private String id;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Column(name = "activation_request_id")
    private String activationRequestId;

    @Column(name = "verification_type", nullable = false, length = 10)
    private String verificationType;

    @Column(name = "input_reference", nullable = false)
    private String inputReference;

    @Column(name = "result_status", length = 20)
    private String resultStatus;

    @Column(name = "ktrn_msgcode", length = 8)
    private String ktrnMsgcode;

    @Column(name = "failure_layer", length = 20)
    private String failureLayer;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
