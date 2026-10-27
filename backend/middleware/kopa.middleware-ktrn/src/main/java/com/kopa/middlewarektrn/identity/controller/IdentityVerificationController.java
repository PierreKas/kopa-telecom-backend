package com.kopa.middlewarektrn.identity.controller;

import com.kopa.middlewarektrn.identity.dto.NidaVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.NidaVerificationResponse;
import com.kopa.middlewarektrn.identity.dto.PassportVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.PassportVerificationResponse;
import com.kopa.middlewarektrn.identity.dto.RraVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.RraVerificationResponse;
import com.kopa.middlewarektrn.identity.service.IdentityVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn/identity")
@Tag(name = "KTRN Identity Verification", description = "HP00005/HPOP303 NIDA, HPOP305 RRA, "
        + "HPOP306 DGIE - internal, called only by kopa.customer-identity during onboarding KYC.")
public class IdentityVerificationController {

    private final IdentityVerificationService identityVerificationService;

    public IdentityVerificationController(IdentityVerificationService identityVerificationService) {
        this.identityVerificationService = identityVerificationService;
    }

    @PostMapping("/verify-nida")
    @Operation(summary = "Verify a Rwandan citizen via NIDA (HPOP303)")
    public ResponseEntity<NidaVerificationResponse> verifyNida(@Valid @RequestBody NidaVerificationRequest request) {
        return ResponseEntity.ok(identityVerificationService.verifyNida(request));
    }

    @PostMapping("/verify-rra")
    @Operation(summary = "Verify a company by TIN via RRA (HPOP305)")
    public ResponseEntity<RraVerificationResponse> verifyRra(@Valid @RequestBody RraVerificationRequest request) {
        return ResponseEntity.ok(identityVerificationService.verifyRra(request));
    }

    @PostMapping("/verify-passport")
    @Operation(summary = "Verify a foreigner by passport number via DGIE (HPOP306)")
    public ResponseEntity<PassportVerificationResponse> verifyPassport(@Valid @RequestBody PassportVerificationRequest request) {
        return ResponseEntity.ok(identityVerificationService.verifyPassport(request));
    }
}
