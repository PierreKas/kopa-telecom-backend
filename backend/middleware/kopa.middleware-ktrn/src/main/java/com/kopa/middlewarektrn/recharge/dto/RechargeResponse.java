package com.kopa.middlewarektrn.recharge.dto;

/** RBSS-CCBS API_ACCESS s2.2.1/s2.2.2/s2.2.3 all document a header-only response (svccode,
 *  opcode, chcode, msgcode, message) - no body fields at all, unlike balance/bundle/usage. */
public record RechargeResponse(
        String msgcode,
        String message
) {
}
