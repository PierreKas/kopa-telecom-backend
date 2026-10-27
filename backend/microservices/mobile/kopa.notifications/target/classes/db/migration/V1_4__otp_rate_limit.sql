SET search_path TO notifications;

-- Protects /internal/v1/otp/send from runaway SMS cost if a caller bug spams one msisdn - a plain
-- fixed-window counter, reset by window_start. notifications.sms doesn't own OTP code generation
-- or verification itself (that stays local to whichever service needs to check it - see
-- OtpSendRequest javadoc), this table only limits how often *this* endpoint will fire per number.
CREATE TABLE otp_rate_limit (
    id           VARCHAR(64) PRIMARY KEY,
    msisdn       VARCHAR(15) NOT NULL,
    window_start TIMESTAMPTZ NOT NULL,
    count        INT NOT NULL DEFAULT 0,
    UNIQUE (msisdn)
);
