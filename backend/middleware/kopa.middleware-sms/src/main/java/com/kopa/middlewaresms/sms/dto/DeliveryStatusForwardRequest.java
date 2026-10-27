package com.kopa.middlewaresms.sms.dto;

public record DeliveryStatusForwardRequest(
        String messageId,
        String status
) {
}
