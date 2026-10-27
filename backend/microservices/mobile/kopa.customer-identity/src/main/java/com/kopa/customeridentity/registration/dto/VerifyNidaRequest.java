package com.kopa.customeridentity.registration.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyNidaRequest(
        @NotBlank String nationalId,
        @NotBlank String photoBase64
) {
}
