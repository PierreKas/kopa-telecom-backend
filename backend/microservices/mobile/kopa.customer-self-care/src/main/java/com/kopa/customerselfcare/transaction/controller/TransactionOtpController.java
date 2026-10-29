package com.kopa.customerselfcare.transaction.controller;

import com.kopa.customerselfcare.auth.dto.CallerContext;
import com.kopa.customerselfcare.common.dto.ApiResponse;
import com.kopa.customerselfcare.transaction.dto.TransactionOtpVerifyRequest;
import com.kopa.customerselfcare.transaction.dto.TransactionOtpVerifyResponse;
import com.kopa.customerselfcare.transaction.service.TransactionOtpVerifyService;
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
@RequestMapping("/api/v1/transactions")
@Validated
@Tag(name = "Transaction OTP", description = "FR-04 - completes an OTP-gated recharge/bundle "
        + "purchase. Client (mobile app) facing only, authenticated.")
public class TransactionOtpController {

    private final TransactionOtpVerifyService verifyService;

    public TransactionOtpController(TransactionOtpVerifyService verifyService) {
        this.verifyService = verifyService;
    }

    @PostMapping("/{msisdn}/{transactionId}/otp/verify")
    @Operation(summary = "Verify the OTP and complete the transaction")
    public ResponseEntity<ApiResponse<TransactionOtpVerifyResponse>> verify(
            CallerContext caller,
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn,
            @PathVariable String transactionId,
            @Valid @RequestBody TransactionOtpVerifyRequest request) {
        caller.requireOwnMsisdn(msisdn);
        return ResponseEntity.ok(ApiResponse.ok(
                verifyService.verifyAndComplete(msisdn, transactionId, request.code())));
    }
}
