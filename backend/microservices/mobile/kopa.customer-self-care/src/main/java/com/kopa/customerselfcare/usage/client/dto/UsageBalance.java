package com.kopa.customerselfcare.usage.client.dto;

/** Mirrors middleware's UsageBalance - one DATA/SMS/VOICE entry from a purchased bundle's
 *  balance breakdown. */
public record UsageBalance(
        String category,
        String allowance,
        String usage,
        String remaining
) {
}
