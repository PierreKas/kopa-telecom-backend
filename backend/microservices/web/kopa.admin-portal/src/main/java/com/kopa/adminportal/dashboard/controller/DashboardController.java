package com.kopa.adminportal.dashboard.controller;

import com.kopa.adminportal.admin.dto.AdminCallerContext;
import com.kopa.adminportal.common.dto.ApiResponse;
import com.kopa.adminportal.dashboard.dto.DashboardFailuresDto;
import com.kopa.adminportal.dashboard.dto.DashboardSummaryDto;
import com.kopa.adminportal.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "FR-14 - today's registrations/transactions/SMS "
        + "delivery rate, and a failure breakdown by layer for support triage.")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Today's registrations, transactions, and SMS delivery rate")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> summary(AdminCallerContext caller) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.summary()));
    }

    @GetMapping("/failures")
    @Operation(summary = "Failure breakdown by layer, for support triage")
    public ResponseEntity<ApiResponse<DashboardFailuresDto>> failures(AdminCallerContext caller) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.failures()));
    }
}
