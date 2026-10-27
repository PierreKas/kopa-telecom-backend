package com.kopa.customeridentity.auth.client;

public record OtpSendRequest(
        String msisdn,
        String code,
        String purpose,
        String correlationId,
        String requestedByService
) {
}
