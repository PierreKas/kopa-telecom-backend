package com.kopa.customerselfcare.recharge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RechargeRequestDto(
        @NotBlank String nationalId,
        @NotBlank String approvalNo,
        @NotNull @Positive BigDecimal amount
) {
}
