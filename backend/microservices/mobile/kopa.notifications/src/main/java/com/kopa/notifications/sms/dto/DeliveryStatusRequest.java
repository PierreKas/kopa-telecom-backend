package com.kopa.notifications.sms.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryStatusRequest(
        @NotBlank String messageId,
        @NotBlank String status
) {
}
