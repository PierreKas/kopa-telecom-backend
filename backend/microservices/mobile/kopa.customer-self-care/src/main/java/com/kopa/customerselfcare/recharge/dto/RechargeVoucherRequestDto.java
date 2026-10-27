package com.kopa.customerselfcare.recharge.dto;

import jakarta.validation.constraints.NotBlank;

public record RechargeVoucherRequestDto(
        @NotBlank String voucherNumber
) {
}
