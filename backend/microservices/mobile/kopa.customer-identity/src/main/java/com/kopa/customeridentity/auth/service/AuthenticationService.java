package com.kopa.customeridentity.auth.service;

import com.kopa.customeridentity.auth.dto.CallerContext;
import com.kopa.customeridentity.auth.model.AuthToken;

import java.util.Optional;

public interface AuthenticationService {

    /** Resolve a raw {@code Authorization} header value to its caller, or empty if the token is
     *  missing, malformed, unknown, revoked, expired, or belongs to an inactive customer. */
    Optional<CallerContext> resolveCaller(String authorizationHeader);

    AuthToken issueToken(String customerId);

    void revokeToken(String rawToken);

    int revokeAllTokens(String customerId);
}
