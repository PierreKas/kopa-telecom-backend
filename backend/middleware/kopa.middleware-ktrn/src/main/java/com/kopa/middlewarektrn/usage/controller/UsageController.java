package com.kopa.middlewarektrn.usage.controller;

import com.kopa.middlewarektrn.usage.dto.UsageSearchRequest;
import com.kopa.middlewarektrn.usage.dto.UsageSearchResponse;
import com.kopa.middlewarektrn.usage.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn/usage")
@Tag(name = "KTRN Usage", description = "HP00001/HPOP008 - internal, called only by kopa.customer-self-care.")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @PostMapping("/search")
    @Operation(summary = "Search usage/remaining allowances for active bundles (HPOP008)")
    public ResponseEntity<UsageSearchResponse> searchUsage(@Valid @RequestBody UsageSearchRequest request) {
        return ResponseEntity.ok(usageService.searchUsage(request));
    }
}
