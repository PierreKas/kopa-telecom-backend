package com.kopa.middlewarektrn.activation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.5.1 - Request searching price plan (HP00005/HPOP301). */
public record PricePlanRequest(

        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @NotBlank
        @Pattern(regexp = "\\d{10,19}", message = "iccid must be 10-19 digits")
        String iccid
) {
}
