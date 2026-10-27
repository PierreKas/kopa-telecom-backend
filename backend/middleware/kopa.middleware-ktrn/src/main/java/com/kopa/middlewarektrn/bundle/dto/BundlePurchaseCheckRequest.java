package com.kopa.middlewarektrn.bundle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.4.1 - Request Bundle Purchase Checking (HP00005/HPOP403). */
public record BundlePurchaseCheckRequest(
        @NotBlank String soId,
        @NotBlank String productCode,

        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn
) {
}
