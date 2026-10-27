package com.kopa.middlewarektrn.identity.dto;

import jakarta.validation.constraints.NotBlank;

/** RBSS-CCBS API_ACCESS s2.5.4 - Get company information with TIN from RRA (HP00005/HPOP305). */
public record RraVerificationRequest(
        @NotBlank String tin,
        @NotBlank String registerId
) {
}
