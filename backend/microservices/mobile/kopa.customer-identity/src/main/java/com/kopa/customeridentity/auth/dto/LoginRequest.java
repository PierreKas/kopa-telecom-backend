package com.kopa.customeridentity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "pin must be exactly 6 digits")
        String pin
) {
}
