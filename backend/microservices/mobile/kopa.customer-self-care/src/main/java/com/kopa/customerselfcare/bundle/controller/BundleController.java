package com.kopa.customerselfcare.bundle.controller;

import com.kopa.customerselfcare.auth.dto.CallerContext;
import com.kopa.customerselfcare.bundle.dto.BundleListResponseDto;
import com.kopa.customerselfcare.bundle.dto.PurchaseBundleRequest;
import com.kopa.customerselfcare.bundle.dto.PurchaseBundleResponse;
import com.kopa.customerselfcare.bundle.service.BundleService;
import com.kopa.customerselfcare.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bundles")
@Validated
@Tag(name = "Bundles", description = "FR-07/FR-08 - browse and purchase bundles (HP00001/HPOP005, "
        + "HP00005/HPOP402, HP00005/HPOP403, HP00002/HPOP004). Client (mobile app) facing only, "
        + "authenticated. Purchase is OTP-gated (FR-04) - returns OTP_PENDING, confirm with POST "
        + "/api/v1/transactions/{msisdn}/{id}/otp/verify.")
public class BundleController {

    private final BundleService bundleService;

    public BundleController(BundleService bundleService) {
        this.bundleService = bundleService;
    }

    @GetMapping("/{msisdn}")
    @Operation(summary = "List bundles available for purchase on a subscriber's line")
    public ResponseEntity<ApiResponse<BundleListResponseDto>> listAvailable(
            CallerContext caller,
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn) {
        caller.requireOwnMsisdn(msisdn);
        return ResponseEntity.ok(ApiResponse.ok(bundleService.listAvailable(msisdn)));
    }

    @PostMapping("/{msisdn}/purchase")
    @Operation(summary = "Purchase a bundle (limit-checked, e-wallet/purchase-checked against KTRN)")
    public ResponseEntity<ApiResponse<PurchaseBundleResponse>> purchase(
            CallerContext caller,
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn,
            @Valid @RequestBody PurchaseBundleRequest request) {
        caller.requireOwnMsisdn(msisdn);
        return ResponseEntity.ok(ApiResponse.ok(bundleService.purchase(msisdn, request)));
    }
}
