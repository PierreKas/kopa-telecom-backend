package com.kopa.customeridentity.registration.dto;

public record VerifyRraResponseDto(
        boolean verified,
        String supplierName,
        String province,
        String district,
        String sector,
        String cell
) {
}
