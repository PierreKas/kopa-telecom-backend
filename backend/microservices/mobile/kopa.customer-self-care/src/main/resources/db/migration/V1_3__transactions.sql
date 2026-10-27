SET search_path TO customer_self_care;

-- Every money-moving operation (recharge, voucher recharge, cancel, bundle purchase) in one
-- unified table. product_code is nullable by design: a plain amount recharge has none, a bundle
-- purchase does. See db-diagram.md customer_self_care s2.
CREATE TABLE transactions (
    id               VARCHAR(64) PRIMARY KEY,
    msisdn           VARCHAR(15) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    product_code     VARCHAR(20),
    amount           NUMERIC(15, 2),
    currency         VARCHAR(3) NOT NULL DEFAULT 'RWF',
    ktrn_appr_no     VARCHAR(50),
    status           VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    ktrn_msgcode     VARCHAR(8),
    ktrn_message     VARCHAR(128),
    failure_layer    VARCHAR(20),
    failure_detail   TEXT,
    correlation_id   VARCHAR(64),
    requested_at     TIMESTAMPTZ NOT NULL,
    completed_at     TIMESTAMPTZ,
    duration_ms      BIGINT
);

CREATE INDEX idx_transactions_msisdn ON transactions (msisdn);
CREATE INDEX idx_transactions_requested_at ON transactions (requested_at);
