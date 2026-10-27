package com.kopa.customeridentity.registration.service;

import com.kopa.customeridentity.auth.dto.LoginResponse;
import com.kopa.customeridentity.auth.model.Customer;
import com.kopa.customeridentity.auth.model.CustomerStatus;
import com.kopa.customeridentity.auth.repository.CustomerRepository;
import com.kopa.customeridentity.auth.service.AuthService;
import com.kopa.customeridentity.common.CorrelationIdHolder;
import com.kopa.customeridentity.common.exception.BadRequestException;
import com.kopa.customeridentity.common.exception.ConflictException;
import com.kopa.customeridentity.common.exception.ServiceUnavailableException;
import com.kopa.customeridentity.config.KopaBusinessProperties;
import com.kopa.customeridentity.events.CustomerActivatedEvent;
import com.kopa.customeridentity.events.EventPublisher;
import com.kopa.customeridentity.config.RabbitConfig;
import com.kopa.customeridentity.registration.client.MiddlewareKtrnClient;
import com.kopa.customeridentity.registration.client.dto.MsisdnStatusRequest;
import com.kopa.customeridentity.registration.client.dto.MsisdnStatusResponse;
import com.kopa.customeridentity.registration.client.dto.MsisdnSuggestionsRequest;
import com.kopa.customeridentity.registration.client.dto.MsisdnSuggestionsResponse;
import com.kopa.customeridentity.registration.client.dto.NidaVerificationRequest;
import com.kopa.customeridentity.registration.client.dto.NidaVerificationResponse;
import com.kopa.customeridentity.registration.client.dto.PassportVerificationRequest;
import com.kopa.customeridentity.registration.client.dto.PassportVerificationResponse;
import com.kopa.customeridentity.registration.client.dto.PricePlanResponse;
import com.kopa.customeridentity.registration.client.dto.RraVerificationRequest;
import com.kopa.customeridentity.registration.client.dto.RraVerificationResponse;
import com.kopa.customeridentity.registration.client.dto.SimpleActivationRequest;
import com.kopa.customeridentity.registration.client.dto.SimpleActivationResponse;
import com.kopa.customeridentity.registration.client.dto.SubscribeCustomerRequest;
import com.kopa.customeridentity.registration.client.dto.SubscribeCustomerResponse;
import com.kopa.customeridentity.registration.dto.ActivateRequest;
import com.kopa.customeridentity.registration.dto.ActivateResponse;
import com.kopa.customeridentity.registration.dto.MsisdnStatusResponseDto;
import com.kopa.customeridentity.registration.dto.MsisdnSuggestionsResponseDto;
import com.kopa.customeridentity.registration.dto.PricePlanDto;
import com.kopa.customeridentity.registration.dto.PricePlanListResponseDto;
import com.kopa.customeridentity.registration.dto.PricePlanRequest;
import com.kopa.customeridentity.registration.dto.VerifyNidaRequest;
import com.kopa.customeridentity.registration.dto.VerifyNidaResponseDto;
import com.kopa.customeridentity.registration.dto.VerifyPassportRequest;
import com.kopa.customeridentity.registration.dto.VerifyPassportResponseDto;
import com.kopa.customeridentity.registration.dto.VerifyRraRequest;
import com.kopa.customeridentity.registration.dto.VerifyRraResponseDto;
import com.kopa.customeridentity.registration.model.ActivationRequest;
import com.kopa.customeridentity.registration.model.ActivationStatus;
import com.kopa.customeridentity.registration.model.KycVerificationLog;
import com.kopa.customeridentity.registration.repository.ActivationRequestRepository;
import com.kopa.customeridentity.registration.repository.KycVerificationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final DateTimeFormatter BIRTH_DAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MiddlewareKtrnClient middlewareClient;
    private final KopaBusinessProperties businessProperties;
    private final CustomerRepository customers;
    private final ActivationRequestRepository activationRequests;
    private final KycVerificationLogRepository kycLogs;
    private final AuthService authService;
    private final EventPublisher eventPublisher;

    public RegistrationServiceImpl(MiddlewareKtrnClient middlewareClient, KopaBusinessProperties businessProperties,
                                   CustomerRepository customers, ActivationRequestRepository activationRequests,
                                   KycVerificationLogRepository kycLogs, AuthService authService,
                                   EventPublisher eventPublisher) {
        this.middlewareClient = middlewareClient;
        this.businessProperties = businessProperties;
        this.customers = customers;
        this.activationRequests = activationRequests;
        this.kycLogs = kycLogs;
        this.authService = authService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PricePlanListResponseDto searchPricePlans(PricePlanRequest request) {
        PricePlanResponse response = middlewareClient.searchPricePlans(
                new com.kopa.customeridentity.registration.client.dto.PricePlanRequest(request.msisdn(), request.iccid()));
        return new PricePlanListResponseDto(
                PricePlanDto.zip(response.productCodes(), response.productNames(), response.productGroups()));
    }

    @Override
    @Transactional
    public VerifyNidaResponseDto verifyNida(String msisdn, VerifyNidaRequest request) {
        NidaVerificationResponse response = middlewareClient.verifyNida(
                new NidaVerificationRequest(request.nationalId(), request.photoBase64()));

        boolean verified = "Y".equalsIgnoreCase(response.result());
        logKyc(msisdn, "NIDA", request.nationalId(), verified ? "VERIFIED" : "NOT_VERIFIED");

        return new VerifyNidaResponseDto(verified, response.name(), response.birthday(), response.gender());
    }

    @Override
    @Transactional
    public VerifyRraResponseDto verifyRra(String msisdn, VerifyRraRequest request) {
        RraVerificationResponse response = middlewareClient.verifyRra(new RraVerificationRequest(request.tin(), "CCBS"));

        // RRA's own body carries a business MESSAGECODE distinct from the outer header msgcode -
        // the doc's only example shows "1" for success; a populated supplier name is the more
        // reliable signal since the exact code taxonomy isn't documented beyond that one example.
        boolean verified = response.supplierName() != null && !response.supplierName().isBlank();
        logKyc(msisdn, "RRA", request.tin(), verified ? "VERIFIED" : "NOT_VERIFIED");

        return new VerifyRraResponseDto(verified, response.supplierName(), response.province(),
                response.district(), response.sector(), response.cell());
    }

    @Override
    @Transactional
    public VerifyPassportResponseDto verifyPassport(String msisdn, VerifyPassportRequest request) {
        PassportVerificationResponse response = middlewareClient.verifyPassport(
                new PassportVerificationRequest(request.docNumber(), request.issuedBy(), "CCBS"));

        boolean verified = response.firstName() != null && !response.firstName().isBlank();
        logKyc(msisdn, "DGIE", request.docNumber(), verified ? "VERIFIED" : "NOT_VERIFIED");

        return new VerifyPassportResponseDto(verified, response.firstName(), response.lastName(),
                response.dateOfBirth(), response.nationality(), response.sex());
    }

    @Override
    @Transactional
    public ActivateResponse activate(ActivateRequest request) {
        if (customers.existsByMsisdn(request.msisdn())) {
            throw new ConflictException("A customer is already registered for this MSISDN.");
        }

        ActivationRequest activation = new ActivationRequest();
        activation.setId(UUID.randomUUID().toString());
        activation.setMsisdn(request.msisdn());
        activation.setIccid(request.iccid());
        activation.setProductCode(request.productCode());
        activation.setStatus(ActivationStatus.PENDING);
        activation.setCorrelationId(CorrelationIdHolder.current());
        activationRequests.save(activation);

        String custId;
        try {
            // LOGIN_ID must be omitted on HPOP501 (subscribe-customer) - live-tested 2026-10-27:
            // sending it at all (any value, including the real TRUPOC credential) fails with a
            // generic -4003 "Error occured when querying.", while omitting it entirely succeeds.
            // This is the opposite of HPOP302 below, where LOGIN_ID is required.
            SubscribeCustomerResponse subscribeResponse = middlewareClient.subscribeCustomer(new SubscribeCustomerRequest(
                    businessProperties.getSoId(), null, businessProperties.getOrgId(),
                    request.custType(), request.fullName(), request.corpRegNo(), request.bizRegNo(),
                    request.birthDay(), request.email(), request.msisdn(), request.addressDetail()));

            custId = subscribeResponse.custId();
            if (custId == null || custId.isBlank()) {
                // KTRN omits body fields entirely on a business error rather than sending them
                // empty - a missing CUST_ID here means subscribe-customer didn't succeed.
                throw new BadRequestException("KTRN did not accept the subscription (no CUST_ID returned) - "
                        + "check custType/corpRegNo/bizRegNo requirements.");
            }
        } catch (BadRequestException | ServiceUnavailableException ex) {
            failActivation(activation, ex instanceof ServiceUnavailableException ? "MIDDLEWARE" : "KTRN_BUSINESS", ex.getMessage());
            throw ex;
        }

        try {
            SimpleActivationResponse activationResponse = middlewareClient.simpleActivate(new SimpleActivationRequest(
                    businessProperties.getSoId(), request.msisdn(), request.productCode(),
                    businessProperties.getLoginId(), businessProperties.getOrgId(), request.iccid(), custId));

            if (activationResponse.result() == null || activationResponse.result().isBlank()) {
                throw new BadRequestException("KTRN did not confirm activation (no RESULT returned).");
            }
        } catch (BadRequestException | ServiceUnavailableException ex) {
            failActivation(activation, ex instanceof ServiceUnavailableException ? "MIDDLEWARE" : "KTRN_BUSINESS", ex.getMessage());
            throw ex;
        }

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setMsisdn(request.msisdn());
        customer.setIccid(request.iccid());
        customer.setCustId(custId);
        customer.setCustType(request.custType());
        customer.setFullName(request.fullName());
        customer.setBirthDay(parseBirthDay(request.birthDay()));
        customer.setEmail(request.email());
        customer.setAddressDetail(request.addressDetail());
        customer.setSoId(businessProperties.getSoId());
        customer.setStatus(CustomerStatus.ACTIVE);
        customers.save(customer);

        activation.setStatus(ActivationStatus.ACTIVATED);
        activationRequests.save(activation);

        LoginResponse session = authService.issueSessionForNewCustomer(customer);

        eventPublisher.publish(RabbitConfig.ROUTING_KEY_CUSTOMER_ACTIVATED, new CustomerActivatedEvent(
                customer.getId(), customer.getMsisdn(), customer.getFullName(), CorrelationIdHolder.current()));

        return new ActivateResponse(customer.getId(), customer.getMsisdn(), customer.getStatus().name(),
                session.token(), session.expiresAt());
    }

    @Override
    public MsisdnStatusResponseDto checkMsisdnStatus(String msisdn) {
        MsisdnStatusResponse response = middlewareClient.checkMsisdnStatus(
                new MsisdnStatusRequest(msisdn, businessProperties.getSoId()));
        return new MsisdnStatusResponseDto(response.available());
    }

    @Override
    public MsisdnSuggestionsResponseDto suggestMsisdns() {
        MsisdnSuggestionsResponse response = middlewareClient.suggestMsisdns(
                new MsisdnSuggestionsRequest(businessProperties.getSoId()));
        return new MsisdnSuggestionsResponseDto(response.msisdns());
    }

    private void logKyc(String msisdn, String type, String inputReference, String resultStatus) {
        KycVerificationLog log = new KycVerificationLog();
        log.setId(UUID.randomUUID().toString());
        log.setMsisdn(msisdn);
        log.setVerificationType(type);
        log.setInputReference(inputReference);
        log.setResultStatus(resultStatus);
        log.setCorrelationId(CorrelationIdHolder.current());
        kycLogs.save(log);
    }

    private void failActivation(ActivationRequest activation, String failureLayer, String failureDetail) {
        activation.setStatus(ActivationStatus.FAILED);
        activation.setFailureLayer(failureLayer);
        activation.setFailureDetail(failureDetail);
        activationRequests.save(activation);
    }

    private static LocalDate parseBirthDay(String birthDay) {
        return LocalDate.parse(birthDay, BIRTH_DAY_FORMAT);
    }
}
