package com.kopa.customeridentity.auth.service;

import com.kopa.customeridentity.auth.dto.CallerContext;
import com.kopa.customeridentity.auth.model.AuthToken;
import com.kopa.customeridentity.auth.model.Customer;
import com.kopa.customeridentity.auth.repository.AuthTokenRepository;
import com.kopa.customeridentity.auth.repository.CustomerRepository;
import com.kopa.customeridentity.config.Consts;
import com.kopa.customeridentity.general.Hash;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthTokenRepository authTokens;
    private final CustomerRepository customers;

    public AuthenticationServiceImpl(AuthTokenRepository authTokens, CustomerRepository customers) {
        this.authTokens = authTokens;
        this.customers = customers;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CallerContext> resolveCaller(String authorizationHeader) {
        if (!Hash.isValidBearerAuthToken(authorizationHeader)) {
            return Optional.empty();
        }
        String rawToken = Hash.getBearerAuthenticationData(authorizationHeader);
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        return authTokens.findById(rawToken)
                .filter(t -> t.isUsable(now))
                .flatMap(t -> customers.findById(t.getCustomerId()))
                .filter(Customer::isActive)
                .map(this::toCaller);
    }

    @Override
    @Transactional
    public AuthToken issueToken(String customerId) {
        Instant now = Instant.now();
        AuthToken token = new AuthToken();
        token.setToken(Hash.generateAuthToken(String.valueOf(System.nanoTime()), customerId));
        token.setCustomerId(customerId);
        token.setIssuedAt(now);
        token.setExpiresAt(now.plus(Consts.AUTH_TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS));
        token.setRevoked(false);
        return authTokens.save(token);
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) {
        authTokens.findById(rawToken).ifPresent(t -> {
            t.setRevoked(true);
            authTokens.save(t);
        });
    }

    @Override
    @Transactional
    public int revokeAllTokens(String customerId) {
        return authTokens.revokeAllForCustomer(customerId);
    }

    private CallerContext toCaller(Customer customer) {
        return new CallerContext(customer.getId(), customer.getMsisdn(), customer.isActive());
    }
}
