package com.kopa.customerselfcare.auth.service;

import com.kopa.customerselfcare.auth.dto.CallerContext;
import com.kopa.customerselfcare.auth.model.AuthToken;
import com.kopa.customerselfcare.auth.model.Customer;
import com.kopa.customerselfcare.auth.repository.AuthTokenRepository;
import com.kopa.customerselfcare.auth.repository.CustomerRepository;
import com.kopa.customerselfcare.general.Hash;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    private CallerContext toCaller(Customer customer) {
        return new CallerContext(customer.getId(), customer.getMsisdn(), customer.isActive());
    }
}
