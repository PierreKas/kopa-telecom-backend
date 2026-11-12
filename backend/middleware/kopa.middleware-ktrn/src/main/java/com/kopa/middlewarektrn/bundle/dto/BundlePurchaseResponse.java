package com.kopa.middlewarektrn.bundle.dto;

/** RBSS-CCBS API_ACCESS s2.3.1 response - RESULT is described as "OK : SUCCESS" in the doc.
 *  msgcode/message are passed through so the caller can surface the real KTRN rejection reason
 *  (e.g. "Not sufficient balance.") instead of a generic failure - same passthrough convention as
 *  EWalletCheckResponse/BundlePurchaseCheckResponse/BundleListResponse in this same package. */
public record BundlePurchaseResponse(
        String result,
        String msgcode,
        String message
) {
}
