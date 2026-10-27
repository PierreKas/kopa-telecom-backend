package com.kopa.notifications.otp.controller;

import com.kopa.notifications.otp.dto.OtpSendRequest;
import com.kopa.notifications.otp.dto.OtpSendResponse;
import com.kopa.notifications.otp.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/otp")
@Tag(name = "OTP", description = "Sync - sends an OTP SMS and returns immediately with "
        + "success/failure. Called by customer-identity (PIN reset) and customer-self-care "
        + "(transaction authorization). Does not generate or verify the code itself - the caller "
        + "owns that state locally.")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    @Operation(summary = "Dispatch an OTP SMS")
    public ResponseEntity<OtpSendResponse> send(@Valid @RequestBody OtpSendRequest request) {
        return ResponseEntity.ok(otpService.send(request));
    }
}
