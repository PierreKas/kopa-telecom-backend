package com.kopa.middlewarektrn.msisdn.controller;

import com.kopa.middlewarektrn.msisdn.dto.MsisdnStatusRequest;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnStatusResponse;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnSuggestionsRequest;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnSuggestionsResponse;
import com.kopa.middlewarektrn.msisdn.service.MsisdnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn/msisdn")
@Tag(name = "KTRN MSISDN", description = "HP00005/HPOP601 status check, HPOP602 suggestions - "
        + "internal, called only by kopa.customer-identity during onboarding.")
public class MsisdnController {

    private final MsisdnService msisdnService;

    public MsisdnController(MsisdnService msisdnService) {
        this.msisdnService = msisdnService;
    }

    @PostMapping("/status")
    @Operation(summary = "Check whether an MSISDN is available (HPOP601)")
    public ResponseEntity<MsisdnStatusResponse> checkStatus(@Valid @RequestBody MsisdnStatusRequest request) {
        return ResponseEntity.ok(msisdnService.checkStatus(request));
    }

    @PostMapping("/suggestions")
    @Operation(summary = "Recommend 5 available MSISDNs for a retailer (HPOP602)")
    public ResponseEntity<MsisdnSuggestionsResponse> recommend(@Valid @RequestBody MsisdnSuggestionsRequest request) {
        return ResponseEntity.ok(msisdnService.recommend(request));
    }
}
