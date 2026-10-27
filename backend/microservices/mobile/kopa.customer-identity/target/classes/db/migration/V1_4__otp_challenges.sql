SET search_path TO customer_identity;

-- PIN_RESET challenges only for now (TRANSACTION_AUTH is handled locally on
-- customer_self_care.transactions instead - see db-diagram.md notes on why that split exists).
CREATE TABLE otp_challenges (
    id             VARCHAR(64) PRIMARY KEY,
    customer_id    VARCHAR(64) NOT NULL REFERENCES customers (id),
    purpose        VARCHAR(20) NOT NULL,
    code_hash      VARCHAR(100) NOT NULL,
    msisdn         VARCHAR(15) NOT NULL,
    attempt_count  INT NOT NULL DEFAULT 0,
    expires_at     TIMESTAMPTZ NOT NULL,
    consumed_at    TIMESTAMPTZ,
    correlation_id VARCHAR(64),
    created_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_otp_challenges_customer_id_purpose ON otp_challenges (customer_id, purpose);
