package com.kopa.customeridentity.auth.controller;

import com.kopa.customeridentity.auth.dto.CallerContext;
import com.kopa.customeridentity.auth.dto.ChangePinRequest;
import com.kopa.customeridentity.auth.dto.LoginRequest;
import com.kopa.customeridentity.auth.dto.LoginResponse;
import com.kopa.customeridentity.auth.dto.PinResetConfirm;
import com.kopa.customeridentity.auth.dto.PinResetRequest;
import com.kopa.customeridentity.auth.dto.SetPinRequest;
import com.kopa.customeridentity.auth.service.AuthService;
import com.kopa.customeridentity.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Login with MSISDN + 6-digit PIN, PIN set/change/reset, logout. "
        + "PIN reset sends an OTP via kopa.notifications (InTouch SMS gateway).")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with MSISDN + 6-digit PIN")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request.msisdn(), request.pin())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the current session")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("Authorization"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pin/set")
    @Operation(summary = "First-time PIN set-up, right after activation")
    public ResponseEntity<Void> setPin(CallerContext caller, @Valid @RequestBody SetPinRequest request) {
        authService.setPin(caller, request.pin());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pin/change")
    @Operation(summary = "Change PIN (requires the current PIN)")
    public ResponseEntity<Void> changePin(CallerContext caller, @Valid @RequestBody ChangePinRequest request) {
        authService.changePin(caller, request.currentPin(), request.newPin());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pin/reset/request")
    @Operation(summary = "Forgot PIN - sends an OTP to the account's MSISDN")
    public ResponseEntity<Void> requestPinReset(@Valid @RequestBody PinResetRequest request) {
        authService.requestPinReset(request.msisdn());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pin/reset/confirm")
    @Operation(summary = "Forgot PIN - verify the OTP and set a new PIN")
    public ResponseEntity<Void> confirmPinReset(@Valid @RequestBody PinResetConfirm request) {
        authService.confirmPinReset(request.msisdn(), request.code(), request.newPin());
        return ResponseEntity.noContent().build();
    }
}
