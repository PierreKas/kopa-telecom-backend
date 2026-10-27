package com.kopa.customeridentity.auth.service;

import com.kopa.customeridentity.auth.client.NotificationsClient;
import com.kopa.customeridentity.auth.client.OtpSendRequest;
import com.kopa.customeridentity.auth.client.OtpSendResponse;
import com.kopa.customeridentity.auth.dto.CallerContext;
import com.kopa.customeridentity.auth.dto.LoginResponse;
import com.kopa.customeridentity.auth.model.AuthToken;
import com.kopa.customeridentity.auth.model.Credential;
import com.kopa.customeridentity.auth.model.Customer;
import com.kopa.customeridentity.auth.model.OtpChallenge;
import com.kopa.customeridentity.auth.model.OtpPurpose;
import com.kopa.customeridentity.auth.repository.CredentialRepository;
import com.kopa.customeridentity.auth.repository.CustomerRepository;
import com.kopa.customeridentity.auth.repository.OtpChallengeRepository;
import com.kopa.customeridentity.common.CorrelationIdHolder;
import com.kopa.customeridentity.common.exception.BadRequestException;
import com.kopa.customeridentity.common.exception.ConflictException;
import com.kopa.customeridentity.common.exception.NotFoundException;
import com.kopa.customeridentity.common.exception.UnauthorizedException;
import com.kopa.customeridentity.config.Consts;
import com.kopa.customeridentity.general.Hash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    /** Failed PIN attempts before a temporary lockout (mirrors typical PIN-based mobile money apps). */
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_MINUTES = 5;
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final CustomerRepository customers;
    private final CredentialRepository credentials;
    private final OtpChallengeRepository otpChallenges;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authentication;
    private final NotificationsClient notificationsClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(CustomerRepository customers, CredentialRepository credentials,
                       OtpChallengeRepository otpChallenges, PasswordEncoder passwordEncoder,
                       AuthenticationService authentication, NotificationsClient notificationsClient) {
        this.customers = customers;
        this.credentials = credentials;
        this.otpChallenges = otpChallenges;
        this.passwordEncoder = passwordEncoder;
        this.authentication = authentication;
        this.notificationsClient = notificationsClient;
    }

    @Transactional
    public LoginResponse login(String msisdn, String rawPin) {
        Customer customer = customers.findByMsisdn(msisdn)
                .orElseThrow(() -> new UnauthorizedException(Consts.BAD_CREDENTIALS_MESSAGE));

        Credential credential = credentials.findByCustomerId(customer.getId())
                .orElseThrow(() -> new UnauthorizedException(Consts.BAD_CREDENTIALS_MESSAGE));

        Instant now = Instant.now();
        if (credential.isLocked(now)) {
            throw new UnauthorizedException("Too many failed attempts. Try again later.");
        }

        if (!passwordEncoder.matches(rawPin, credential.getPinHash())) {
            registerFailedAttempt(credential, now);
            throw new UnauthorizedException(Consts.BAD_CREDENTIALS_MESSAGE);
        }

        if (!customer.isActive()) {
            throw new UnauthorizedException("This account is not active.");
        }

        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);
        credentials.save(credential);

        return issueSession(customer);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String raw = Hash.getBearerAuthenticationData(authorizationHeader);
        if (raw != null && !raw.isBlank()) {
            authentication.revokeToken(raw);
        }
    }

    /** First-time PIN set-up, right after activation - the caller already holds a session token
     *  issued at activation time (see RegistrationServiceImpl), so this is an authenticated call
     *  even though no PIN has ever been set yet. Rejects if a PIN already exists (use change). */
    @Transactional
    public void setPin(CallerContext caller, String pin) {
        if (credentials.findByCustomerId(caller.customerId()).isPresent()) {
            throw new ConflictException("A PIN is already set for this account. Use change-pin instead.");
        }
        Credential credential = new Credential();
        credential.setCustomerId(caller.customerId());
        credential.setPinHash(passwordEncoder.encode(pin));
        credentials.save(credential);
    }

    @Transactional
    public void changePin(CallerContext caller, String currentPin, String newPin) {
        Credential credential = credentials.findByCustomerId(caller.customerId())
                .orElseThrow(() -> new UnauthorizedException("No PIN set for this account yet."));

        if (!passwordEncoder.matches(currentPin, credential.getPinHash())) {
            throw new BadRequestException("Current PIN is incorrect.");
        }
        if (passwordEncoder.matches(newPin, credential.getPinHash())) {
            throw new BadRequestException("New PIN must be different from the current one.");
        }

        credential.setPinHash(passwordEncoder.encode(newPin));
        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);
        credentials.save(credential);
    }

    /** Issues a fresh session for a customer with no PIN yet, right after activation succeeds. */
    @Transactional
    public LoginResponse issueSessionForNewCustomer(Customer customer) {
        return issueSession(customer);
    }

    /** "Forgot PIN" step 1 - generates and dispatches an OTP. Deliberately does not reveal whether
     *  the msisdn exists as anything other than a 404, matching this codebase's existing level of
     *  rigor elsewhere (no enumeration-hardening attempted yet). */
    @Transactional
    public void requestPinReset(String msisdn) {
        Customer customer = customers.findByMsisdn(msisdn)
                .orElseThrow(() -> new NotFoundException("No account found for this number."));

        String code = generateOtpCode();
        String correlationId = CorrelationIdHolder.current();
        Instant now = Instant.now();

        OtpChallenge challenge = new OtpChallenge();
        challenge.setId(UUID.randomUUID().toString());
        challenge.setCustomerId(customer.getId());
        challenge.setPurpose(OtpPurpose.PIN_RESET);
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setMsisdn(msisdn);
        challenge.setExpiresAt(now.plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES));
        challenge.setCorrelationId(correlationId);
        challenge.setCreatedAt(now);
        otpChallenges.save(challenge);

        OtpSendResponse response = notificationsClient.sendOtp(new OtpSendRequest(
                msisdn, code, "PIN_RESET", correlationId, "customer-identity"));
        if (!response.success()) {
            throw new BadRequestException("Could not send the verification code: " + response.errorMessage());
        }
    }

    /** "Forgot PIN" step 2 - verifies the OTP and sets the new PIN. */
    @Transactional
    public void confirmPinReset(String msisdn, String code, String newPin) {
        Customer customer = customers.findByMsisdn(msisdn)
                .orElseThrow(() -> new NotFoundException("No account found for this number."));

        OtpChallenge challenge = otpChallenges
                .findTopByCustomerIdAndPurposeOrderByCreatedAtDesc(customer.getId(), OtpPurpose.PIN_RESET)
                .orElseThrow(() -> new BadRequestException("No verification code was requested for this number."));

        if (challenge.isConsumed()) {
            throw new BadRequestException("This verification code has already been used.");
        }
        if (challenge.isExpired(Instant.now())) {
            throw new BadRequestException("This verification code has expired. Request a new one.");
        }
        if (challenge.getAttemptCount() >= OTP_MAX_ATTEMPTS) {
            throw new BadRequestException("Too many incorrect attempts. Request a new code.");
        }

        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            challenge.setAttemptCount(challenge.getAttemptCount() + 1);
            otpChallenges.save(challenge);
            throw new BadRequestException("Incorrect verification code.");
        }

        challenge.setConsumedAt(Instant.now());
        otpChallenges.save(challenge);

        Credential credential = credentials.findByCustomerId(customer.getId()).orElseGet(() -> {
            Credential fresh = new Credential();
            fresh.setCustomerId(customer.getId());
            return fresh;
        });
        credential.setPinHash(passwordEncoder.encode(newPin));
        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);
        credentials.save(credential);
    }

    private String generateOtpCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }

    private void registerFailedAttempt(Credential credential, Instant now) {
        int attempts = credential.getFailedAttempts() + 1;
        credential.setFailedAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            credential.setLockedUntil(now.plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES));
        }
        credentials.save(credential);
    }

    private LoginResponse issueSession(Customer customer) {
        AuthToken token = authentication.issueToken(customer.getId());
        return new LoginResponse(customer.getId(), customer.getMsisdn(), token.getToken(), token.getExpiresAt());
    }
}
