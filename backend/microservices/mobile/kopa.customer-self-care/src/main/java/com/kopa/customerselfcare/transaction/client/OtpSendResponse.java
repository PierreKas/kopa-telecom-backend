package com.kopa.customerselfcare.transaction.client;

public record OtpSendResponse(
        boolean success,
        String messageId,
        String errorMessage
) {
}
