package com.kopa.middlewarektrn.identity.dto;

import jakarta.validation.constraints.NotBlank;

/** RBSS-CCBS API_ACCESS s2.5.5 - Get foreigner information with passport number from DGIE
 *  (HP00005/HPOP306). Fingerprints are optional per the doc; omitted here since Kopa's mobile
 *  app onboarding flow has no fingerprint capture. */
public record PassportVerificationRequest(
        @NotBlank String docNumber,
        @NotBlank String issuedBy,
        @NotBlank String registerId
) {
}
