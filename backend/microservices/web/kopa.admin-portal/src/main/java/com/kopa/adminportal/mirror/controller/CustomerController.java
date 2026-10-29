package com.kopa.adminportal.mirror.controller;

import com.kopa.adminportal.admin.dto.AdminCallerContext;
import com.kopa.adminportal.common.dto.ApiResponse;
import com.kopa.adminportal.mirror.dto.CustomerDto;
import com.kopa.adminportal.mirror.service.CustomerLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/customers")
@Tag(name = "Admin Customers", description = "FR-14 - search/view subscribers. Read-only mirror "
        + "of customer_identity.customers, never written here.")
public class CustomerController {

    private final CustomerLookupService customerLookupService;

    public CustomerController(CustomerLookupService customerLookupService) {
        this.customerLookupService = customerLookupService;
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
