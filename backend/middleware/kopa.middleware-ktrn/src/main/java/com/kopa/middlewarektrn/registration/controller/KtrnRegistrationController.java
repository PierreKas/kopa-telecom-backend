package com.kopa.middlewarektrn.registration.controller;

import com.kopa.middlewarektrn.registration.dto.SearchCustomerRequest;
import com.kopa.middlewarektrn.registration.dto.SearchCustomerResponse;
import com.kopa.middlewarektrn.registration.dto.SearchMsisdnByIccidRequest;
import com.kopa.middlewarektrn.registration.dto.SearchMsisdnByIccidResponse;
import com.kopa.middlewarektrn.registration.dto.SubscribeCustomerRequest;
import com.kopa.middlewarektrn.registration.dto.SubscribeCustomerResponse;
import com.kopa.middlewarektrn.registration.service.KtrnRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn/registration")
@Tag(name = "KTRN Registration", description = "HP00005/HPOP501 subscribe customer, HPOP502 search "
        + "customer, HP00001/HPOP003 search MSISDN by ICCID - internal, called only by kopa.customer-identity.")
public class KtrnRegistrationController {

    private final KtrnRegistrationService registrationService;

    public KtrnRegistrationController(KtrnRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/subscribe-customer")
    @Operation(summary = "Register a new customer with KTRN (HPOP501)")
    public ResponseEntity<SubscribeCustomerResponse> subscribeCustomer(@Valid @RequestBody SubscribeCustomerRequest request) {
        return ResponseEntity.ok(registrationService.subscribeCustomer(request));
    }

    @PostMapping("/search-customer")
    @Operation(summary = "Search an existing KTRN customer (HPOP502)")
    public ResponseEntity<SearchCustomerResponse> searchCustomer(@Valid @RequestBody SearchCustomerRequest request) {
        return ResponseEntity.ok(registrationService.searchCustomer(request));
    }

    @PostMapping("/search-msisdn-by-iccid")
    @Operation(summary = "Search MSISDN/retailer by ICCID (HPOP003)")
    public ResponseEntity<SearchMsisdnByIccidResponse> searchMsisdnByIccid(@Valid @RequestBody SearchMsisdnByIccidRequest request) {
        return ResponseEntity.ok(registrationService.searchMsisdnByIccid(request));
    }
}
