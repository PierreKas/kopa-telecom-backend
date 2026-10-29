package com.kopa.customerselfcare.auth.service;

import com.kopa.customerselfcare.auth.dto.CallerContext;

import java.util.Optional;

public interface AuthenticationService {

    /** Validates the Authorization header against the mirrored auth_tokens/customers tables.
     *  Empty if the header is missing/malformed, the token is unknown/expired/revoked, or the
     *  customer it belongs to isn't ACTIVE. */
    Optional<CallerContext> resolveCaller(String authorizationHeader);
}
