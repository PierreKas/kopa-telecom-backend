package com.kopa.middlewaresms.sms.dto;

public record SendSmsResponse(
        boolean success,
        String messageId,
        String status,
        Double cost,
        String balance,
        String errorMessage
) {
}
