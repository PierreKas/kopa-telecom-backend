package com.kopa.customeridentity.auth.dto;

import java.time.Instant;

public record LoginResponse(
        String customerId,
        String msisdn,
        String token,
        Instant expiresAt
) {
}
