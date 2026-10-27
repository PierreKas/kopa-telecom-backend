package com.kopa.notifications.sms.model;

public enum SmsMessageStatus {
    QUEUED,
    SENT,
    DELIVERED,
    UNDELIVERED,
    ERRORED,
    FAILED_TO_SEND
}
