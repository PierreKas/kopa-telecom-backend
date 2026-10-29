package com.kopa.customerselfcare.auth.dto;

import com.kopa.customerselfcare.common.exception.ForbiddenException;

/** "Who is calling, right now" - resolved once per request by TokenValidationFilter, re-derived
 *  by controllers via CallerContextArgumentResolver. Same pattern as kopa.customer-identity's own
 *  CallerContext. */
public record CallerContext(
        String customerId,
        String msisdn,
        boolean active
) {

    /** Every endpoint here is keyed by an {msisdn} path variable - this is the one check that
     *  stops an authenticated subscriber from acting on someone else's line. */
    public void requireOwnMsisdn(String requestedMsisdn) {
        if (!msisdn.equals(requestedMsisdn)) {
            throw new ForbiddenException("You can only access your own account.");
        }
    }
}
