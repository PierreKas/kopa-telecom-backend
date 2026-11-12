package com.kopa.middlewarektrn.usage.dto;

import java.util.List;

/** One record from PURCHASED_LIST: "PRODUCT CODE, PRODUCT NAME, PRODUCT FLAG(V-BUNDLE,B-BASE),
 *  PACKAGE PRICE, EFF_DATETIME, EXP_DATETIME, {ALL_BAL_LIST}" (RBSS-CCBS v2.0 s2.1.2).
 *
 *  2026-10-29: this replaces an earlier, wrong field mapping that read the trailing brace-wrapped
 *  balance array as flat comma fields - landing PACKAGE PRICE/EFF_DATETIME/EXP_DATETIME into
 *  allowance/usage/remaining instead of the real numbers, which live one level deeper inside
 *  {ALL_BAL_LIST}. Live-confirmed against a real purchased bundle once one existed to compare
 *  against the doc's schema. Only productCode/productName/balances are kept here - flag/price/
 *  the two top-level dates aren't consumed by anything today. */
public record UsageItem(
        String productCode,
        String productName,
        List<UsageBalance> balances
) {
}
