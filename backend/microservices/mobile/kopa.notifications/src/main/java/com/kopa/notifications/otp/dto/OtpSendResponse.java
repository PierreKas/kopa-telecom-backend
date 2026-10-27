package com.kopa.notifications.otp.dto;

public record OtpSendResponse(
        boolean success,
        String messageId,
        String errorMessage
) {
}
