SET search_path TO notifications;

-- "No domain data of its own, it's a send-log" - same shape as digi.sante.notifications.notification_log.
-- Every send (OTP or event-triggered) goes through SmsDispatchService and writes exactly one row here.
CREATE TABLE sms_messages (
    id                    VARCHAR(64) PRIMARY KEY,
    correlation_id        VARCHAR(64),
    msisdn                VARCHAR(15) NOT NULL,
    purpose               VARCHAR(30) NOT NULL,
    template_code         VARCHAR(50) NOT NULL,
    rendered_message      TEXT NOT NULL,
    provider_message_id   VARCHAR(50),
    status                VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    cost                  NUMERIC(10, 2),
    failure_layer         VARCHAR(20),
    failure_detail        TEXT,
    requested_by_service  VARCHAR(50) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_sms_messages_msisdn ON sms_messages (msisdn);
CREATE INDEX idx_sms_messages_provider_message_id ON sms_messages (provider_message_id);
