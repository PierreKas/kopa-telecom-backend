package com.kopa.customeridentity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PinResetConfirm(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "code must be exactly 6 digits")
        String code,

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "newPin must be exactly 6 digits")
        String newPin
) {
}
