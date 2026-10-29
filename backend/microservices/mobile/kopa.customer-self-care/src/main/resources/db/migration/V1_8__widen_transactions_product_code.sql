SET search_path TO customer_self_care;

-- Live-confirmed 2026-10-28: KTRN's own PROD_CD values aren't short codes - "KOPA
-- TelecomMainProduct" (23 chars) already exceeds the original VARCHAR(20), and failed the INSERT
-- in TransactionFailureRecorder.recordFailure() with "value too long for type character varying
-- (20)" - which masked the real, correctly-classified BadRequestException behind a raw 500.
ALTER TABLE transactions ALTER COLUMN product_code TYPE VARCHAR(100);
