package com.kopa.customeridentity.auth.client;

public record OtpSendResponse(
        boolean success,
        String messageId,
        String errorMessage
) {
}
