package com.kopa.adminportal.admin.controller;

import com.kopa.adminportal.admin.dto.BootstrapAdminRequest;
import com.kopa.adminportal.admin.dto.LoginRequest;
import com.kopa.adminportal.admin.dto.LoginResponse;
import com.kopa.adminportal.admin.service.AdminAuthService;
import com.kopa.adminportal.common.dto.ApiResponse;
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
@RequestMapping("/api/v1/admin/auth")
@Tag(name = "Admin Auth", description = "FR-13 - admin login, own credential space (never "
        + "resolves a Mobile bearer token). /bootstrap is guarded by X-Internal-Api-Key and only "
        + "ever runs once - see AdminAuthServiceImpl.bootstrap().")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/bootstrap")
    @Operation(summary = "Create the first admin account - requires X-Internal-Api-Key, runs only once")
    public ResponseEntity<Void> bootstrap(@Valid @RequestBody BootstrapAdminRequest request) {
        adminAuthService.bootstrap(request.username(), request.password());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with username + password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminAuthService.login(request.username(), request.password())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the current session")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        adminAuthService.logout(request.getHeader("Authorization"));
        return ResponseEntity.noContent().build();
    }
}
