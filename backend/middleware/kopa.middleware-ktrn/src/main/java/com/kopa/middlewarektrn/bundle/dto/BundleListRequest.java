package com.kopa.middlewarektrn.bundle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.1.3 - Searching available bundles (HP00001/HPOP005). */
public record BundleListRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @Pattern(regexp = "\\d{1,3}", message = "ispCd must be 1-3 digits")
        String ispCd
) {
}
