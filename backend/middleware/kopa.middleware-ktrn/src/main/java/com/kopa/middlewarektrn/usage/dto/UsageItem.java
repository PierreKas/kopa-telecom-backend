package com.kopa.middlewarektrn.usage.dto;

/** One record from PURCHASED_LIST: "PRODUCT CODE,PRODUCT NAME,PRODUCT FLAG(V-BUNDLE,B-BASE),
 *  ALLOWANCE,USAGE,REMAINING". */
public record UsageItem(
        String productCode,
        String productName,
        String flag,
        String allowance,
        String usage,
        String remaining
) {
}
