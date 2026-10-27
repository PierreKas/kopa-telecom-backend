package com.kopa.middlewarektrn.balance.dto;

/** Raw KTRN outcome, header and body fields flattened. msgcode/message are passed through
 *  as-is (1001 = success, see API_ACCESS s1.1 Common Message table for the rest) - interpreting
 *  them into HTTP semantics is the calling microservice's job, not this bridge's. */
public record BalanceLookupResponse(
        String svccode,
        String opcode,
        String chcode,
        String msgcode,
        String message,
        String remainingAmount
) {
}
