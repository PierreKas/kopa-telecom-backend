package com.kopa.adminportal.mirror.controller;

import com.kopa.adminportal.admin.dto.AdminCallerContext;
import com.kopa.adminportal.common.dto.ApiResponse;
import com.kopa.adminportal.mirror.dto.TransactionDto;
import com.kopa.adminportal.mirror.service.TransactionLookupService;
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
@RequestMapping("/api/v1/admin/transactions")
@Tag(name = "Admin Transactions", description = "FR-14 - search/view recharge/bundle-purchase "
        + "transactions, including failure_layer/failure_detail for support triage. Read-only "
        + "mirror of customer_self_care.transactions, never written here.")
public class TransactionController {

    private final TransactionLookupService transactionLookupService;

    public TransactionController(TransactionLookupService transactionLookupService) {
        this.transactionLookupService = transactionLookupService;
    }

    @GetMapping
    @Operation(summary = "Search/list transactions by MSISDN and/or status")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> search(
            AdminCallerContext caller,
            @RequestParam(required = false) String msisdn,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(transactionLookupService.search(msisdn, status)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Transaction detail")
    public ResponseEntity<ApiResponse<TransactionDto>> getById(AdminCallerContext caller, @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(transactionLookupService.getById(id)));
    }
}
