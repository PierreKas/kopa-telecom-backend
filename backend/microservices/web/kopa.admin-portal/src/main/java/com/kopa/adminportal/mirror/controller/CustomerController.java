package com.kopa.adminportal.mirror.controller;

import com.kopa.adminportal.admin.dto.ActivateSimRequest;
import com.kopa.adminportal.admin.dto.ActivateSimResponse;
import com.kopa.adminportal.admin.dto.AdminCallerContext;
import com.kopa.adminportal.admin.dto.CreateCustomerRequest;
import com.kopa.adminportal.admin.dto.CreateCustomerResponse;
import com.kopa.adminportal.admin.service.CustomerCreationService;
import com.kopa.adminportal.common.dto.ApiResponse;
import com.kopa.adminportal.mirror.dto.CustomerDto;
import com.kopa.adminportal.mirror.service.CustomerLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/customers")
@Validated
@Tag(name = "Admin Customers", description = "FR-14 - search/view subscribers (read-only mirror "
        + "of customer_identity.customers). POST creates a new customer (PENDING_ACTIVATION); "
        + "its SIM is activated as a separate step (2026-11-03 split) - customers cannot do "
        + "either themselves.")
public class CustomerController {

    private final CustomerLookupService customerLookupService;
    private final CustomerCreationService customerCreationService;

    public CustomerController(CustomerLookupService customerLookupService,
                               CustomerCreationService customerCreationService) {
        this.customerLookupService = customerLookupService;
        this.customerCreationService = customerCreationService;
    }

    @PostMapping
    @Operation(summary = "Create a new customer (PENDING_ACTIVATION, no SIM yet)")
    public ResponseEntity<ApiResponse<CreateCustomerResponse>> create(
            AdminCallerContext caller, @Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(customerCreationService.create(request)));
    }

    @PostMapping("/{id}/activate-sim")
    @Operation(summary = "Activate a SIM for an already-created, PENDING_ACTIVATION customer")
    public ResponseEntity<ApiResponse<ActivateSimResponse>> activateSim(
            AdminCallerContext caller, @PathVariable String id, @Valid @RequestBody ActivateSimRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(customerCreationService.activateSim(id, request)));
    }

    @GetMapping
    @Operation(summary = "Search/list customers by MSISDN or name")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> search(
            AdminCallerContext caller,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(customerLookupService.search(q)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Customer detail")
    public ResponseEntity<ApiResponse<CustomerDto>> getById(AdminCallerContext caller, @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(customerLookupService.getById(id)));
    }
}
