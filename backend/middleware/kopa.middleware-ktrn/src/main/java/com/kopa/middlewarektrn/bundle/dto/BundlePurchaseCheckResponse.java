package com.kopa.middlewarektrn.bundle.dto;

/** RBSS-CCBS API_ACCESS s2.4.1 response. result: "Y" per the doc's example. */
public record BundlePurchaseCheckResponse(
        String result
) {
}
