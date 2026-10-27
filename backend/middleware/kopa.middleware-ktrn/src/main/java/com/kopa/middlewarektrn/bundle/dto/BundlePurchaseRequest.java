package com.kopa.middlewarektrn.bundle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.3.1 - Request Bundle Purchase (HP00002/HPOP004). */
public record BundlePurchaseRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        String ispCd,

        @NotBlank String productCode
) {
}
