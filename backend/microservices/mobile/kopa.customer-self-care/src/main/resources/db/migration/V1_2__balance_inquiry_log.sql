SET search_path TO customer_self_care;

-- Audit trail for feature 2.1.1 (Searching Balance, svccode HP00001 / opcode HPOP001). KTRN is the
-- system of record for the balance itself; this table only records who asked, when, and what came
-- back, for observability/troubleshooting.
CREATE TABLE balance_inquiry_log (
    id               UUID PRIMARY KEY,
    msisdn           VARCHAR(15) NOT NULL,
    isp_cd           VARCHAR(3),
    requested_at     TIMESTAMPTZ NOT NULL,
    success          BOOLEAN NOT NULL,
    ktrn_msgcode     VARCHAR(5),
    ktrn_message     VARCHAR(100),
    remaining_amount VARCHAR(15),
    error_detail     VARCHAR(255),
    duration_ms      BIGINT
);

CREATE INDEX idx_balance_inquiry_log_msisdn ON balance_inquiry_log (msisdn);
CREATE INDEX idx_balance_inquiry_log_requested_at ON balance_inquiry_log (requested_at);
