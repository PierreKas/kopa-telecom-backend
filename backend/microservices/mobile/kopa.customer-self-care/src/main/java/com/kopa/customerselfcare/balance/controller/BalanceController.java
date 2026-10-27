package com.kopa.customerselfcare.balance.controller;

import com.kopa.customerselfcare.balance.dto.BalanceDto;
import com.kopa.customerselfcare.balance.service.BalanceInquiryService;
import com.kopa.customerselfcare.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/balance")
@Validated
@Tag(name = "Balance", description = "Feature 2.1.1 Searching Balance - svccode HP00001 / opcode "
        + "HPOP001. Client (mobile app) facing only; no authentication yet (planned for a later "
        + "phase - see kopa.customer-self-care README/architecture notes).")
public class BalanceController {

    private final BalanceInquiryService balanceInquiryService;

    public BalanceController(BalanceInquiryService balanceInquiryService) {
        this.balanceInquiryService = balanceInquiryService;
    }

    @GetMapping("/{msisdn}")
    @Operation(summary = "Search a subscriber's balance by MSISDN")
    public ResponseEntity<ApiResponse<BalanceDto>> searchBalance(
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn,
            @RequestParam(required = false)
            @Parameter(description = "Business No defined by ORN (KTRN ISP_CD). Falls back to the "
                    + "middleware's configured default when omitted.")
            @Pattern(regexp = "\\d{1,3}", message = "ispCd must be 1-3 digits")
            String ispCd) {
        return ResponseEntity.ok(ApiResponse.ok(balanceInquiryService.searchBalance(msisdn, ispCd)));
    }
}
