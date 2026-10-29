package com.kopa.adminportal.mirror.model;

/** Mirrors kopa.notifications.sms.model.SmsMessageStatus exactly. */
public enum SmsMessageStatus {
    QUEUED,
    SENT,
    DELIVERED,
    UNDELIVERED,
    ERRORED,
    FAILED_TO_SEND
}
