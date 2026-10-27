package com.kopa.middlewarektrn.identity.dto;

/** RBSS-CCBS API_ACCESS s2.5.5 response. */
public record PassportVerificationResponse(
        String status,
        String message,
        String personId,
        String firstName,
        String lastName,
        String dateOfBirth,
        String docNumber,
        String issuedBy,
        String nationality,
        String expiryDate,
        String sex
) {
}
