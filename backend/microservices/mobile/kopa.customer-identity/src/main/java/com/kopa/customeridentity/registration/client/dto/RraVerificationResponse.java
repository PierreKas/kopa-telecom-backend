package com.kopa.customeridentity.registration.client.dto;

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
