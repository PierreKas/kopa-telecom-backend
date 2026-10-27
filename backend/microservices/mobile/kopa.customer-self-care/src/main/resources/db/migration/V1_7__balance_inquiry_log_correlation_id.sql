SET search_path TO customer_self_care;

-- balance_inquiry_log predates the correlation_id convention (feature 2.1.1 was built before it
-- existed) - every other "attempt" table already has one. Bringing it in line.
ALTER TABLE balance_inquiry_log
    ADD COLUMN correlation_id VARCHAR(64);
