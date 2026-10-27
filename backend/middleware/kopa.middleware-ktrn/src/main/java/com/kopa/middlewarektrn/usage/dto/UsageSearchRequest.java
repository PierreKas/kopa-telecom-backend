package com.kopa.middlewarektrn.usage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.1.4 - Searching usage balance (HP00001/HPOP008). */
public record UsageSearchRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @Pattern(regexp = "\\d{1,3}", message = "ispCd must be 1-3 digits")
        String ispCd
) {
}
