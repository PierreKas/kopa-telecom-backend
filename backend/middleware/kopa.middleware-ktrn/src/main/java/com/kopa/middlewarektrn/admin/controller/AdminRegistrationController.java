package com.kopa.middlewarektrn.admin.controller;

import com.kopa.middlewarektrn.admin.dto.AdminActivateResponse;
import com.kopa.middlewarektrn.admin.dto.AdminActivateSimRequest;
import com.kopa.middlewarektrn.admin.dto.AdminCreateCustomerRequest;
import com.kopa.middlewarektrn.admin.service.AdminRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn/admin")
@Validated
@Tag(name = "KTRN Admin Registration", description = "HPOP313/601/501/502/302 - admin-portal"
        + "-driven customer create+activate, split into two independent steps (2026-11-03). "
        + "Internal, called only by kopa.customer-identity.")
public class AdminRegistrationController {

    private final AdminRegistrationService service;

    public AdminRegistrationController(AdminRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/create-customer")
    @Operation(summary = "Create a new customer on behalf of the shared agent account (no SIM activation)")
    public ResponseEntity<AdminActivateResponse> createCustomer(@Valid @RequestBody AdminCreateCustomerRequest request) {
        return ResponseEntity.ok(service.createCustomer(request));
    }

    @PostMapping("/activate-sim")
    @Operation(summary = "Activate a SIM for an already-created customer")
    public ResponseEntity<AdminActivateResponse> activateSim(@Valid @RequestBody AdminActivateSimRequest request) {
        return ResponseEntity.ok(service.activateSim(request));
    }
}
