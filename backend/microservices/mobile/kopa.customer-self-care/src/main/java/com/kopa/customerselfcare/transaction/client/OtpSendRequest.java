package com.kopa.customerselfcare.transaction.client;

public record OtpSendRequest(
        String msisdn,
        String code,
        String purpose,
        String correlationId,
        String requestedByService
) {
}
