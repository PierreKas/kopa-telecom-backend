package com.kopa.customerselfcare.usage.controller;

import com.kopa.customerselfcare.common.dto.ApiResponse;
import com.kopa.customerselfcare.usage.dto.UsageResponseDto;
import com.kopa.customerselfcare.usage.service.UsageInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usage")
@Validated
@Tag(name = "Usage", description = "FR-06 - usage/remaining allowances for active bundles "
        + "(HP00001/HPOP008). Client (mobile app) facing only.")
public class UsageController {

    private final UsageInquiryService usageInquiryService;

    public UsageController(UsageInquiryService usageInquiryService) {
        this.usageInquiryService = usageInquiryService;
    }

    @GetMapping("/{msisdn}")
    @Operation(summary = "Search usage/remaining allowances for a subscriber's active bundles")
    public ResponseEntity<ApiResponse<UsageResponseDto>> searchUsage(
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn) {
        return ResponseEntity.ok(ApiResponse.ok(usageInquiryService.searchUsage(msisdn)));
    }
}
