package com.kopa.middlewarektrn.msisdn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.7.1 - Checking MSISDN status (HP00005/HPOP601). */
public record MsisdnStatusRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @NotBlank String soId
) {
}
