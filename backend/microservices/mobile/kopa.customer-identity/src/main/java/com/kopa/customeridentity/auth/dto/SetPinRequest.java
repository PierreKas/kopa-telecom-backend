package com.kopa.customeridentity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** First-time PIN set-up, right after activation - no current PIN to check yet. */
public record SetPinRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "pin must be exactly 6 digits")
        String pin
) {
}
