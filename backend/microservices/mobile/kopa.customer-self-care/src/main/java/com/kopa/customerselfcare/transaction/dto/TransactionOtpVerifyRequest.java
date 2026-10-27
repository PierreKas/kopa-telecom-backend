package com.kopa.customerselfcare.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TransactionOtpVerifyRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "code must be exactly 6 digits")
        String code
) {
}
