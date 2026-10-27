SET search_path TO customer_self_care;

-- Read-only inquiries (usage, bundle browsing) - no money moved, so not a transactions row.
-- Separate from balance_inquiry_log (feature 2.1.1) rather than merged into it, to avoid
-- touching already-tested code - see ServiceInquiryLog's javadoc.
CREATE TABLE service_inquiry_log (
    id             VARCHAR(64) PRIMARY KEY,
    msisdn         VARCHAR(15) NOT NULL,
    inquiry_type   VARCHAR(20) NOT NULL,
    success        BOOLEAN NOT NULL,
    failure_layer  VARCHAR(20),
    correlation_id VARCHAR(64),
    requested_at   TIMESTAMPTZ NOT NULL,
    duration_ms    BIGINT
);

CREATE INDEX idx_service_inquiry_log_msisdn ON service_inquiry_log (msisdn);
