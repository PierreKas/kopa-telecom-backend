package com.kopa.customerselfcare.recharge.controller;

import com.kopa.customerselfcare.auth.dto.CallerContext;
import com.kopa.customerselfcare.common.dto.ApiResponse;
import com.kopa.customerselfcare.recharge.dto.RechargeCancelRequestDto;
import com.kopa.customerselfcare.recharge.dto.RechargeRequestDto;
import com.kopa.customerselfcare.recharge.dto.RechargeResponseDto;
import com.kopa.customerselfcare.recharge.dto.RechargeVoucherRequestDto;
import com.kopa.customerselfcare.recharge.service.RechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recharge")
@Validated
@Tag(name = "Recharge", description = "FR-09 - recharge by amount/voucher, cancel "
        + "(HP00002/HPOP001, HPOP002, HPOP003). Client (mobile app) facing only, authenticated. "
        + "Recharge by amount is OTP-gated (FR-04) - returns OTP_PENDING, confirm with POST "
        + "/api/v1/transactions/{msisdn}/{id}/otp/verify. Voucher recharge and cancel are not "
        + "OTP-gated. No Mobile Money integration yet (Scope of Work s4.2) - approvalNo is a "
        + "placeholder field for that future step.")
public class RechargeController {

    private final RechargeService rechargeService;

    public RechargeController(RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }

    @PostMapping("/{msisdn}")
    @Operation(summary = "Recharge by amount (limit-checked)")
    public ResponseEntity<ApiResponse<RechargeResponseDto>> recharge(
            CallerContext caller,
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn,
            @Valid @RequestBody RechargeRequestDto request) {
        caller.requireOwnMsisdn(msisdn);
        return ResponseEntity.ok(ApiResponse.ok(rechargeService.recharge(msisdn, request)));
    }

    @PostMapping("/{msisdn}/voucher")
    @Operation(summary = "Recharge by voucher number")
    public ResponseEntity<ApiResponse<RechargeResponseDto>> rechargeByVoucher(
            CallerContext caller,
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn,
            @Valid @RequestBody RechargeVoucherRequestDto request) {
        caller.requireOwnMsisdn(msisdn);
        return ResponseEntity.ok(ApiResponse.ok(rechargeService.rechargeByVoucher(msisdn, request)));
    }

    @PostMapping("/{msisdn}/{transactionId}/cancel")
    @Operation(summary = "Cancel a previous successful recharge")
    public ResponseEntity<ApiResponse<RechargeResponseDto>> cancel(
            CallerContext caller,
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn,
            @PathVariable String transactionId,
            @Valid @RequestBody RechargeCancelRequestDto request) {
        caller.requireOwnMsisdn(msisdn);
        return ResponseEntity.ok(ApiResponse.ok(rechargeService.cancel(msisdn, transactionId, request)));
    }
}
