package com.kopa.customeridentity.registration.client.dto;

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
