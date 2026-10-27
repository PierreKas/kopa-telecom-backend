package com.kopa.customeridentity.registration.dto;

public record VerifyPassportResponseDto(
        boolean verified,
        String firstName,
        String lastName,
        String dateOfBirth,
        String nationality,
        String sex
) {
}
