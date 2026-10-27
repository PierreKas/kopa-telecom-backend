SET search_path TO customer_self_care;

-- Configurable, not hardcoded - the Scope of Work states limits are a Kopa-provided business
-- rule, so this table is the mechanism. See backend/architecture-reference.md s10.
CREATE TABLE transaction_limit_policy (
    id                  VARCHAR(64) PRIMARY KEY,
    transaction_type    VARCHAR(20) NOT NULL UNIQUE,
    max_amount_per_tx   NUMERIC(15, 2),
    max_amount_per_day  NUMERIC(15, 2),
    max_count_per_day   INT,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at          TIMESTAMPTZ NOT NULL
);

-- Placeholder defaults (Kopa's real business thresholds are not yet confirmed - see
-- backend/architecture-reference.md s13 Open items). RECHARGE_CANCEL deliberately has no policy
-- row: cancels reduce exposure, not increase it, so they're not limited the same way.
INSERT INTO transaction_limit_policy (id, transaction_type, max_amount_per_tx, max_amount_per_day, max_count_per_day, active, updated_at) VALUES
    (gen_random_uuid()::text, 'RECHARGE', 50000, 200000, 10, TRUE, now()),
    (gen_random_uuid()::text, 'RECHARGE_VOUCHER', NULL, NULL, 10, TRUE, now()),
    (gen_random_uuid()::text, 'BUNDLE_PURCHASE', 50000, 200000, 10, TRUE, now());

CREATE TABLE daily_transaction_counter (
    id               VARCHAR(64) PRIMARY KEY,
    msisdn           VARCHAR(15) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    total_amount     NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_count      INT NOT NULL DEFAULT 0,
    UNIQUE (msisdn, transaction_date, transaction_type)
);
