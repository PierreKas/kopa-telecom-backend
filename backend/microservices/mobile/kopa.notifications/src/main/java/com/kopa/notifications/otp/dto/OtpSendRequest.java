package com.kopa.notifications.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * notifications does not generate or own the OTP code itself - that state (hash, expiry, attempt
 * count) stays local to whichever service needs to verify it later: customer_identity.otp_challenges
 * for PIN_RESET, a few extra columns on customer_self_care.transactions for TRANSACTION_AUTH (see
 * db-diagram.md notes). The caller generates the plaintext code and just asks this endpoint to
 * dispatch it as an SMS and log the send - same "no domain data of its own" boundary as every
 * other sms_messages row.
 */
public record OtpSendRequest(
        @NotBlank String msisdn,
        @NotBlank String code,
        @NotNull OtpPurpose purpose,
        String correlationId,
        @NotBlank String requestedByService
) {
}
