package com.kopa.middlewarektrn.identity.dto;

/** RBSS-CCBS API_ACCESS s2.5.4 response - only the fields Kopa's onboarding flow actually needs;
 *  the doc lists several more address-detail fields that aren't modeled here. */
public record RraVerificationResponse(
        String messageCode,
        String messageDescription,
        String supplierName,
        String supplierTin,
        String emailAddress,
        String phoneNumber,
        String province,
        String district,
        String sector,
        String cell,
        String category
) {
}
