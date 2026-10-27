package com.kopa.customerselfcare.balance.dto;

import java.time.Instant;

/** Client (mobile app) facing shape for feature 2.1.1 - not the KTRN/Middleware wire format. */
public record BalanceDto(
        String msisdn,
        String ispCd,
        String remainingAmount,
        String currency,
        Instant retrievedAt
) {
}
