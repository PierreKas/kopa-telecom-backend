package com.kopa.middlewarektrn.balance.controller;

import com.kopa.middlewarektrn.balance.dto.BalanceLookupRequest;
import com.kopa.middlewarektrn.balance.dto.BalanceLookupResponse;
import com.kopa.middlewarektrn.balance.dto.BalanceSummaryRequest;
import com.kopa.middlewarektrn.balance.dto.BalanceSummaryResponse;
import com.kopa.middlewarektrn.balance.service.KtrnBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn")
@Tag(name = "KTRN Balance", description = "Internal bridge to KTRN svccode HP00001 / opcode HPOP001 - "
        + "called only by Kopa's own microservices, never directly by the Client app or Admin portal.")
public class KtrnBalanceController {

    private final KtrnBalanceService balanceService;

    public KtrnBalanceController(KtrnBalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @PostMapping("/balance")
    @Operation(summary = "Search subscriber balance via KTRN (HP00001 / HPOP001)")
    public ResponseEntity<BalanceLookupResponse> checkBalance(@Valid @RequestBody BalanceLookupRequest request) {
        return ResponseEntity.ok(balanceService.checkBalance(request));
    }

    @PostMapping("/balance/summary")
    @Operation(summary = "Richer balance + usage + bundle history via KTRN (HP00005 / HPOP101)")
    public ResponseEntity<BalanceSummaryResponse> checkSummary(@Valid @RequestBody BalanceSummaryRequest request) {
        return ResponseEntity.ok(balanceService.checkSummary(request));
    }
}
