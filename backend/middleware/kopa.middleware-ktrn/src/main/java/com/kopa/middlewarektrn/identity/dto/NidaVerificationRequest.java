package com.kopa.middlewarektrn.identity.dto;

import jakarta.validation.constraints.NotBlank;

/** RBSS-CCBS API_ACCESS s2.5.3 - Request for NIDA getCitizen API (HP00005/HPOP303). */
public record NidaVerificationRequest(
        @NotBlank String nationalId,
        @NotBlank String photoBase64
) {
}
