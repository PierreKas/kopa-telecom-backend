package com.kopa.customerselfcare.recharge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RechargeCancelRequestDto(
        @NotBlank String nationalId,
        @NotNull @Positive BigDecimal cancelAmount
) {
}
