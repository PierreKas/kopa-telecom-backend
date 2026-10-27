package com.kopa.notifications.sms.client;

/** Mirrors kopa.middleware-sms's SendSmsResponse. */
public record MiddlewareSmsResult(
        boolean success,
        String messageId,
        String status,
        Double cost,
        String balance,
        String errorMessage
) {
}
