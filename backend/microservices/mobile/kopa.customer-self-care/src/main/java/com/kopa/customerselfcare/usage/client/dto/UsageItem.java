package com.kopa.customerselfcare.usage.client.dto;

public record UsageItem(
        String productCode,
        String productName,
        String flag,
        String allowance,
        String usage,
        String remaining
) {
}
