package com.kopa.customeridentity.registration.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyRraRequest(
        @NotBlank String tin
) {
}
