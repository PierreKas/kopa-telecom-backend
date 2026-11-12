package com.kopa.customeridentity.auth.dto;

import java.time.Instant;

public record LoginResponse(
        String customerId,
        String msisdn,
        String token,
        Instant expiresAt,
        /** True when this login used the system-generated temporary PIN sent by SMS after admin
         *  activation (2026-11-03) - the app should advise the customer to set a familiar PIN via
         *  change-pin. */
        boolean temporaryPin
) {
}
