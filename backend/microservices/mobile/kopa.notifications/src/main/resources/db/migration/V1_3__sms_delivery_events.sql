SET search_path TO notifications;

-- Append-only: InTouch's DLR can arrive as a sequence of intermediate statuses (P -> Q -> S ->
-- D/U), not a single terminal callback - http_intouch_api_1.2.pdf s3.
CREATE TABLE sms_delivery_events (
    id             VARCHAR(64) PRIMARY KEY,
    sms_message_id VARCHAR(64) NOT NULL REFERENCES sms_messages (id),
    status         VARCHAR(2) NOT NULL,
    raw_payload    TEXT,
    received_at    TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_sms_delivery_events_sms_message_id ON sms_delivery_events (sms_message_id);
