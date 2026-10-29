package com.kopa.customerselfcare.statement.controller;

import com.kopa.customerselfcare.auth.dto.CallerContext;
import com.kopa.customerselfcare.common.dto.ApiResponse;
import com.kopa.customerselfcare.statement.dto.StatementResponseDto;
import com.kopa.customerselfcare.statement.service.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statement")
@Validated
@Tag(name = "Statement", description = "FR-10 - purchase/recharge/usage history. Client (mobile app) facing only, authenticated.")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping("/{msisdn}")
    @Operation(summary = "Transaction history for a subscriber")
    public ResponseEntity<ApiResponse<StatementResponseDto>> getStatement(
            CallerContext caller,
            @PathVariable
            @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
            String msisdn) {
        caller.requireOwnMsisdn(msisdn);
        return ResponseEntity.ok(ApiResponse.ok(statementService.getStatement(msisdn)));
    }
}
