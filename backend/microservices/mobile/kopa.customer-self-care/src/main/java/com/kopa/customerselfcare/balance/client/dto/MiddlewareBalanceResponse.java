package com.kopa.customerselfcare.balance.client.dto;

/** Mirrors kopa.middleware-ktrn's BalanceLookupResponse. */
public record MiddlewareBalanceResponse(
        String svccode,
        String opcode,
        String chcode,
        String msgcode,
        String message,
        String remainingAmount
) {
}
