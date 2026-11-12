package com.kopa.customerselfcare.usage.dto;

/** One DATA/SMS/VOICE balance entry from a purchased bundle - client (mobile app) facing. */
public record UsageBalanceDto(
        String category,
        String allowance,
        String usage,
        String remaining
) {
}
