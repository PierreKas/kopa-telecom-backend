package com.kopa.customeridentity.registration.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyPassportRequest(
        @NotBlank String docNumber,
        @NotBlank String issuedBy
) {
}
