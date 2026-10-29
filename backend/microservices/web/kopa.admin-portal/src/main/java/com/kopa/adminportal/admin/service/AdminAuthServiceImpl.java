package com.kopa.adminportal.admin.service;

import com.kopa.adminportal.admin.dto.AdminCallerContext;
import com.kopa.adminportal.admin.dto.LoginResponse;
import com.kopa.adminportal.admin.model.AdminAuthToken;
import com.kopa.adminportal.admin.model.AdminUser;
import com.kopa.adminportal.admin.repository.AdminAuthTokenRepository;
import com.kopa.adminportal.admin.repository.AdminUserRepository;
import com.kopa.adminportal.common.exception.BadRequestException;
import com.kopa.adminportal.common.exception.ConflictException;
import com.kopa.adminportal.config.Consts;
import com.kopa.adminportal.general.Hash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserRepository adminUsers;
    private final AdminAuthTokenRepository adminAuthTokens;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthServiceImpl(AdminUserRepository adminUsers, AdminAuthTokenRepository adminAuthTokens,
                                 PasswordEncoder passwordEncoder) {
        this.adminUsers = adminUsers;
        this.adminAuthTokens = adminAuthTokens;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void bootstrap(String username, String rawPassword) {
        if (adminUsers.count() > 0) {
            throw new ConflictException("An admin account already exists - this endpoint only runs once.");
        }
        AdminUser admin = new AdminUser();
        admin.setId(UUID.randomUUID().toString());
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        adminUsers.save(admin);
    }

    @Override
    @Transactional
    public LoginResponse login(String username, String rawPassword) {
        AdminUser admin = adminUsers.findByUsername(username)
                .filter(a -> passwordEncoder.matches(rawPassword, a.getPasswordHash()))
                .orElseThrow(() -> new BadRequestException(Consts.BAD_CREDENTIALS_MESSAGE));

        Instant now = Instant.now();
        AdminAuthToken token = new AdminAuthToken();
        token.setToken(Hash.generateAuthToken(String.valueOf(System.nanoTime()), admin.getId()));
        token.setAdminId(admin.getId());
        token.setIssuedAt(now);
        token.setExpiresAt(now.plus(Consts.AUTH_TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS));
        token.setRevoked(false);
        adminAuthTokens.save(token);

        return new LoginResponse(admin.getId(), admin.getUsername(), token.getToken(), token.getExpiresAt());
    }

    @Override
    @Transactional
    public void logout(String authorizationHeader) {
        if (!Hash.isValidBearerAuthToken(authorizationHeader)) {
            return;
        }
        String rawToken = Hash.getBearerAuthenticationData(authorizationHeader);
        adminAuthTokens.findById(rawToken).ifPresent(t -> {
            t.setRevoked(true);
            adminAuthTokens.save(t);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminCallerContext> resolveCaller(String authorizationHeader) {
        if (!Hash.isValidBearerAuthToken(authorizationHeader)) {
            return Optional.empty();
        }
        String rawToken = Hash.getBearerAuthenticationData(authorizationHeader);
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        return adminAuthTokens.findById(rawToken)
                .filter(t -> t.isUsable(now))
                .flatMap(t -> adminUsers.findById(t.getAdminId()))
                .map(admin -> new AdminCallerContext(admin.getId(), admin.getUsername(), admin.getRole()));
    }
}
