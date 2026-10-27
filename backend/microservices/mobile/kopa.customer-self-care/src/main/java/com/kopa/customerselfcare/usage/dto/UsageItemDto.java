package com.kopa.customerselfcare.usage.dto;

public record UsageItemDto(
        String productCode,
        String productName,
        String flag,
        String allowance,
        String usage,
        String remaining
) {
}
