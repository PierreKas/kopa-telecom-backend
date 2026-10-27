SET search_path TO customer_identity;

-- Tracks the multi-step KTRN onboarding workflow for one MSISDN. KTRN is the system of record for
-- the actual subscription; this table is for observability/troubleshooting (which layer a failed
-- activation broke in) - see backend/architecture-reference.md s9.
CREATE TABLE activation_requests (
    id               VARCHAR(64) PRIMARY KEY,
    msisdn           VARCHAR(15) NOT NULL,
    iccid            VARCHAR(19),
    product_code     VARCHAR(20),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ktrn_msgcode     VARCHAR(8),
    ktrn_message     VARCHAR(128),
    failure_layer    VARCHAR(20),
    failure_detail   TEXT,
    correlation_id   VARCHAR(64),
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_activation_requests_msisdn ON activation_requests (msisdn);

-- Deliberately no FK to activation_requests - KYC checks (verify-nida/rra/passport) happen before
-- the /activate call that creates that row, so this is keyed by msisdn instead. See
-- KycVerificationLog's javadoc.
CREATE TABLE kyc_verification_log (
    id                     VARCHAR(64) PRIMARY KEY,
    msisdn                 VARCHAR(15) NOT NULL,
    activation_request_id  VARCHAR(64),
    verification_type      VARCHAR(10) NOT NULL,
    input_reference        VARCHAR(64) NOT NULL,
    result_status          VARCHAR(20),
    ktrn_msgcode           VARCHAR(8),
    failure_layer          VARCHAR(20),
    correlation_id         VARCHAR(64),
    created_at             TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_kyc_verification_log_msisdn ON kyc_verification_log (msisdn);
