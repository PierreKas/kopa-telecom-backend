package com.kopa.customeridentity.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PricePlanRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @NotBlank
        @Pattern(regexp = "\\d{10,19}", message = "iccid must be 10-19 digits")
        String iccid
) {
}
