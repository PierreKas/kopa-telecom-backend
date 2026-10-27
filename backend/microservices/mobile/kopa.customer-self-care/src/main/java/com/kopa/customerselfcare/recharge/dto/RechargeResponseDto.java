package com.kopa.customerselfcare.recharge.dto;

public record RechargeResponseDto(
        String transactionId,
        String status,
        String ktrnMessage
) {
}
