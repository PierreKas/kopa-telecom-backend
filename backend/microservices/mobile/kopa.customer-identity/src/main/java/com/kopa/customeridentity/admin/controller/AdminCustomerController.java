package com.kopa.customeridentity.admin.controller;

import com.kopa.customeridentity.admin.dto.AdminActivateSimRequest;
import com.kopa.customeridentity.admin.dto.AdminActivateSimResponse;
import com.kopa.customeridentity.admin.dto.AdminCreateCustomerRequest;
import com.kopa.customeridentity.admin.dto.AdminCreateCustomerResponse;
import com.kopa.customeridentity.admin.service.AdminCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/admin")
@Validated
@Tag(name = "Admin Customer Creation", description = "Create a customer, then activate their SIM "
        + "as a separate step (2026-11-03 split) - internal, called only by kopa.admin-portal.")
public class AdminCustomerController {

    private final AdminCustomerService service;

    public AdminCustomerController(AdminCustomerService service) {
        this.service = service;
    }

    @PostMapping("/customers")
    @Operation(summary = "Create a new customer (PENDING_ACTIVATION, no SIM yet)")
    public ResponseEntity<AdminCreateCustomerResponse> create(@Valid @RequestBody AdminCreateCustomerRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PostMapping("/customers/{id}/activate-sim")
    @Operation(summary = "Activate a SIM for an already-created, PENDING_ACTIVATION customer")
    public ResponseEntity<AdminActivateSimResponse> activateSim(
            @PathVariable String id, @Valid @RequestBody AdminActivateSimRequest request) {
        return ResponseEntity.ok(service.activateSim(id, request));
    }
}
