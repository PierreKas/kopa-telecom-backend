package com.kopa.customeridentity.registration.dto;

import java.time.Instant;

/** A session is issued immediately on successful activation (token/expiresAt) so the client can
 *  call the authenticated POST /api/v1/auth/pin/set right away, without a separate login step -
 *  there's no PIN to log in with yet. */
public record ActivateResponse(
        String customerId,
        String msisdn,
        String status,
        String token,
        Instant expiresAt
) {
}
