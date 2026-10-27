package com.kopa.customeridentity.auth.dto;

/** "Who is calling, right now" - resolved once per request by TokenValidationFilter, re-derived
 *  by controllers via CallerContextArgumentResolver. Same pattern as digi.sante.auth-access. */
public record CallerContext(
        String customerId,
        String msisdn,
        boolean active
) {
}
