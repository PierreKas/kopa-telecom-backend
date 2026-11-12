package com.kopa.customerselfcare.usage.dto;

import java.util.List;

public record UsageItemDto(
        String productCode,
        String productName,
        List<UsageBalanceDto> balances
) {
}
