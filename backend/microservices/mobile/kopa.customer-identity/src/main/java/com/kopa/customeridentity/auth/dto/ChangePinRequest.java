package com.kopa.customeridentity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePinRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "currentPin must be exactly 6 digits")
        String currentPin,

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "newPin must be exactly 6 digits")
        String newPin
) {
}
