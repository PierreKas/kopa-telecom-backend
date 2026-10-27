package com.kopa.customeridentity.registration.controller;

import com.kopa.customeridentity.registration.dto.ActivateRequest;
import com.kopa.customeridentity.registration.dto.ActivateResponse;
import com.kopa.customeridentity.registration.dto.MsisdnStatusResponseDto;
import com.kopa.customeridentity.registration.dto.MsisdnSuggestionsResponseDto;
import com.kopa.customeridentity.registration.dto.PricePlanListResponseDto;
import com.kopa.customeridentity.registration.dto.PricePlanRequest;
import com.kopa.customeridentity.registration.dto.VerifyNidaRequest;
import com.kopa.customeridentity.registration.dto.VerifyNidaResponseDto;
import com.kopa.customeridentity.registration.dto.VerifyPassportRequest;
import com.kopa.customeridentity.registration.dto.VerifyPassportResponseDto;
import com.kopa.customeridentity.registration.dto.VerifyRraRequest;
import com.kopa.customeridentity.registration.dto.VerifyRraResponseDto;
import com.kopa.customeridentity.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/v1/registration")
@Validated
@Tag(name = "Registration", description = "KTRN-backed onboarding: price plans, KYC verification "
        + "(NIDA/RRA/DGIE), activation. Public - no session exists yet until /activate succeeds.")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/price-plans")
    @Operation(summary = "Search price plans available for simple activation")
    public ResponseEntity<PricePlanListResponseDto> searchPricePlans(@Valid @RequestBody PricePlanRequest request) {
        return ResponseEntity.ok(registrationService.searchPricePlans(request));
    }

    @PostMapping("/verify-nida/{msisdn}")
    @Operation(summary = "KYC: verify a Rwandan citizen via NIDA")
    public ResponseEntity<VerifyNidaResponseDto> verifyNida(
            @PathVariable @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits") String msisdn,
            @Valid @RequestBody VerifyNidaRequest request) {
        return ResponseEntity.ok(registrationService.verifyNida(msisdn, request));
    }

    @PostMapping("/verify-rra/{msisdn}")
    @Operation(summary = "KYC: verify a company by TIN via RRA (corporate onboarding)")
    public ResponseEntity<VerifyRraResponseDto> verifyRra(
            @PathVariable @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits") String msisdn,
            @Valid @RequestBody VerifyRraRequest request) {
        return ResponseEntity.ok(registrationService.verifyRra(msisdn, request));
    }

    @PostMapping("/verify-passport/{msisdn}")
    @Operation(summary = "KYC: verify a foreigner by passport number via DGIE")
    public ResponseEntity<VerifyPassportResponseDto> verifyPassport(
            @PathVariable @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits") String msisdn,
            @Valid @RequestBody VerifyPassportRequest request) {
        return ResponseEntity.ok(registrationService.verifyPassport(msisdn, request));
    }

    @PostMapping("/activate")
    @Operation(summary = "Subscribe + simple-activate a new customer, then issue a session for the "
            + "immediate PIN set-up step")
    public ResponseEntity<ActivateResponse> activate(@Valid @RequestBody ActivateRequest request) {
        return ResponseEntity.ok(registrationService.activate(request));
    }

    @GetMapping("/msisdn-status/{msisdn}")
    @Operation(summary = "Check whether an MSISDN is available")
    public ResponseEntity<MsisdnStatusResponseDto> checkMsisdnStatus(
            @PathVariable @NotBlank @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits") String msisdn) {
        return ResponseEntity.ok(registrationService.checkMsisdnStatus(msisdn));
    }

    @GetMapping("/msisdn-suggestions")
    @Operation(summary = "Recommend 5 available MSISDNs for a new line")
    public ResponseEntity<MsisdnSuggestionsResponseDto> suggestMsisdns() {
        return ResponseEntity.ok(registrationService.suggestMsisdns());
    }
}
