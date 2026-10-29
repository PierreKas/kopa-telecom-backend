package com.kopa.middlewarektrn.bundle.dto;

/** RBSS-CCBS API_ACCESS s2.4.1 response. result: "Y" per the doc's example. msgcode/message are
 *  passed through so the caller can surface the real KTRN rejection reason instead of a generic
 *  "declined" message - same passthrough convention as BalanceLookupResponse. */
public record BundlePurchaseCheckResponse(
        String result,
        String msgcode,
        String message
) {
}
