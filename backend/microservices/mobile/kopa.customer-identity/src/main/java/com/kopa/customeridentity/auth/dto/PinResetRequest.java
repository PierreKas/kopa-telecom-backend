package com.kopa.customeridentity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** "Forgot PIN" - request an OTP be sent before confirming a new PIN with {@link PinResetConfirm}. */
public record PinResetRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn
) {
}
