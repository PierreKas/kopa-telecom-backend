SET search_path TO customer_self_care;

-- OTP-gating for FR-04 (recharge by amount, bundle purchase). national_id/req_approval_no are
-- the RECHARGE request's own fields, nullable - they need to survive between "request OTP" and
-- "verify OTP + actually call KTRN", since the verify call only carries {transactionId, code}.
-- Distinct from ktrn_appr_no, which is KTRN's *response* approval number, not the request's.
ALTER TABLE transactions
    ADD COLUMN otp_code_hash     VARCHAR(100),
    ADD COLUMN otp_expires_at    TIMESTAMPTZ,
    ADD COLUMN otp_attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN national_id       VARCHAR(30),
    ADD COLUMN req_approval_no   VARCHAR(50);
