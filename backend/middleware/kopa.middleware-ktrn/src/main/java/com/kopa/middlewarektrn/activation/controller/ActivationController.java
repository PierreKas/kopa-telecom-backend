package com.kopa.middlewarektrn.activation.controller;

import com.kopa.middlewarektrn.activation.dto.PricePlanRequest;
import com.kopa.middlewarektrn.activation.dto.PricePlanResponse;
import com.kopa.middlewarektrn.activation.dto.SimpleActivationRequest;
import com.kopa.middlewarektrn.activation.dto.SimpleActivationResponse;
import com.kopa.middlewarektrn.activation.service.ActivationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn/activation")
@Tag(name = "KTRN Activation", description = "HP00005/HPOP301 price plans, HPOP302 simple activation - "
        + "internal, called only by kopa.customer-identity.")
public class ActivationController {

    private final ActivationService activationService;

    public ActivationController(ActivationService activationService) {
        this.activationService = activationService;
    }

    @PostMapping("/price-plans")
    @Operation(summary = "Search price plans available for simple activation (HPOP301)")
    public ResponseEntity<PricePlanResponse> searchPricePlans(@Valid @RequestBody PricePlanRequest request) {
        return ResponseEntity.ok(activationService.searchPricePlans(request));
    }

    @PostMapping("/simple-activate")
    @Operation(summary = "Simple activation for a prepay customer (HPOP302)")
    public ResponseEntity<SimpleActivationResponse> simpleActivate(@Valid @RequestBody SimpleActivationRequest request) {
        return ResponseEntity.ok(activationService.simpleActivate(request));
    }
}
