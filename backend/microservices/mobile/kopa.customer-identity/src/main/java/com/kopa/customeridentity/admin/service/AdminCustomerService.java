package com.kopa.customeridentity.admin.service;

import com.kopa.customeridentity.admin.dto.AdminActivateSimRequest;
import com.kopa.customeridentity.admin.dto.AdminActivateSimResponse;
import com.kopa.customeridentity.admin.dto.AdminCreateCustomerRequest;
import com.kopa.customeridentity.admin.dto.AdminCreateCustomerResponse;
import com.kopa.customeridentity.auth.client.NotificationsClient;
import com.kopa.customeridentity.auth.client.OtpSendRequest;
import com.kopa.customeridentity.auth.client.OtpSendResponse;
import com.kopa.customeridentity.auth.model.Credential;
import com.kopa.customeridentity.auth.model.Customer;
import com.kopa.customeridentity.auth.model.CustomerStatus;
import com.kopa.customeridentity.auth.repository.CredentialRepository;
import com.kopa.customeridentity.auth.repository.CustomerRepository;
import com.kopa.customeridentity.common.CorrelationIdHolder;
import com.kopa.customeridentity.common.exception.BadRequestException;
import com.kopa.customeridentity.common.exception.ConflictException;
import com.kopa.customeridentity.common.exception.NotFoundException;
import com.kopa.customeridentity.registration.client.MiddlewareKtrnClient;
import com.kopa.customeridentity.registration.client.dto.AdminActivateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Admin-portal-driven customer creation, <b>split from SIM activation (2026-11-03)</b> - see
 *  AdminRegistrationService on middleware-ktrn for why: HPOP302 (activation) over the KTRN
 *  gateway has been slow/timing out independently of HPOP501 (creation), and collapsing both
 *  into one call meant a slow activation could make a perfectly good creation look like a
 *  total failure. Now a customer can sit in PENDING_ACTIVATION indefinitely - the admin portal
 *  surfaces an "Activate SIM" action for exactly that state, retryable any number of times.
 *
 * <p>The admin never sets or sees the customer's PIN (2026-11-03) - that was flagged as a
 * confidentiality risk. Instead, once SIM activation succeeds, a random 6-digit PIN is
 * generated here, sent by SMS to the now-active number, and valid for 15 minutes; the customer
 * logs in with it and is expected to set a PIN of their own via change-pin. If it expires
 * unused, the normal "Forgot PIN" flow (AuthService.requestPinReset/confirmPinReset) applies -
 * no special handling needed there since it already creates a fresh Credential if none is
 * usable. */
@Service
public class AdminCustomerService {

    private static final Logger logger = LoggerFactory.getLogger(AdminCustomerService.class);
    private static final DateTimeFormatter BIRTH_DAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int INITIAL_PIN_LENGTH = 6;
    private static final int INITIAL_PIN_TTL_MINUTES = 15;

    private final MiddlewareKtrnClient middlewareClient;
    private final CustomerRepository customers;
    private final CredentialRepository credentials;
    private final PasswordEncoder passwordEncoder;
    private final NotificationsClient notificationsClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminCustomerService(MiddlewareKtrnClient middlewareClient, CustomerRepository customers,
                                 CredentialRepository credentials, PasswordEncoder passwordEncoder,
                                 NotificationsClient notificationsClient) {
        this.middlewareClient = middlewareClient;
        this.customers = customers;
        this.credentials = credentials;
        this.passwordEncoder = passwordEncoder;
        this.notificationsClient = notificationsClient;
    }

    @Transactional
    public AdminCreateCustomerResponse create(AdminCreateCustomerRequest request) {
        if (customers.existsByMsisdn(request.msisdn())) {
            throw new ConflictException("A customer already exists for this MSISDN.");
        }

        AdminActivateResponse ktrnResult = middlewareClient.createCustomer(
                new com.kopa.customeridentity.registration.client.dto.AdminCreateCustomerRequest(
                        request.msisdn(), request.custType(), request.fullName(), request.nationalId(),
                        request.birthDay(), request.addressDetail(), request.email(),
                        request.longitude(), request.latitude()));

        if (!ktrnResult.success()) {
            throw new BadRequestException(ktrnResult.message() != null
                    ? ktrnResult.message() : "KTRN declined this registration.");
        }

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setMsisdn(request.msisdn());
        customer.setNationalId(request.nationalId());
        customer.setCustId(ktrnResult.custId());
        customer.setCustType(request.custType());
        customer.setFullName(request.fullName());
        customer.setBirthDay(LocalDate.parse(request.birthDay(), BIRTH_DAY_FORMAT));
        customer.setEmail(request.email());
        customer.setAddressDetail(request.addressDetail());
        customer.setStatus(CustomerStatus.PENDING_ACTIVATION);
        customers.save(customer);

        return new AdminCreateCustomerResponse(customer.getId(), customer.getMsisdn(),
                customer.getCustId(), customer.getStatus().name());
    }

    @Transactional
    public AdminActivateSimResponse activateSim(String customerId, AdminActivateSimRequest request) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new NotFoundException("No customer found with this ID."));
        if (customer.isActive()) {
            throw new ConflictException("This customer's SIM is already activated.");
        }

        AdminActivateResponse ktrnResult = middlewareClient.activateSim(
                new com.kopa.customeridentity.registration.client.dto.AdminActivateSimRequest(
                        customer.getMsisdn(), request.iccid(), customer.getCustId()));

        if (!ktrnResult.success()) {
            throw new BadRequestException(ktrnResult.message() != null
                    ? ktrnResult.message() : "KTRN declined this activation.");
        }

        customer.setIccid(request.iccid());
        customer.setStatus(CustomerStatus.ACTIVE);
        customers.save(customer);

        issueInitialPin(customer);

        return new AdminActivateSimResponse(customer.getId(), customer.getMsisdn(), customer.getStatus().name());
    }

    /** KTRN's activation already succeeded by the time this runs and cannot be undone - an SMS
     *  failure here must never roll back the customer's ACTIVE status (same reasoning as
     *  splitting create/activate in the first place). So this only logs; the customer can always
     *  fall back to "Forgot PIN", which works with zero credentials rows already. */
    private void issueInitialPin(Customer customer) {
        String pin = generateInitialPin();

        Credential credential = new Credential();
        credential.setCustomerId(customer.getId());
        credential.setPinHash(passwordEncoder.encode(pin));
        credential.setPinExpiresAt(Instant.now().plus(INITIAL_PIN_TTL_MINUTES, ChronoUnit.MINUTES));
        credentials.save(credential);

        try {
            OtpSendResponse response = notificationsClient.sendOtp(new OtpSendRequest(
                    customer.getMsisdn(), pin, "INITIAL_PIN", CorrelationIdHolder.current(),
                    "customer-identity", customer.getFullName()));
            if (!response.success()) {
                logger.warn("Initial PIN SMS not sent for customer {}: {}", customer.getId(), response.errorMessage());
            }
        } catch (RuntimeException ex) {
            logger.warn("Initial PIN SMS failed for customer {}: {}", customer.getId(), ex.getMessage());
        }
    }

    private String generateInitialPin() {
        int bound = (int) Math.pow(10, INITIAL_PIN_LENGTH);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + INITIAL_PIN_LENGTH + "d", code);
    }
}
