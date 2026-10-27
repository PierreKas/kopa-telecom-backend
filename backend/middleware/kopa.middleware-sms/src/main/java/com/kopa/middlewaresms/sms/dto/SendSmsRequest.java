package com.kopa.middlewaresms.sms.dto;

import jakarta.validation.constraints.NotBlank;

public record SendSmsRequest(
        @NotBlank String recipients,
        @NotBlank String message,
        String sender,
        String dlrUrl
) {
}
