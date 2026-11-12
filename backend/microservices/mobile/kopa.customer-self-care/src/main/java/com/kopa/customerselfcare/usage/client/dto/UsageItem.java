package com.kopa.customerselfcare.usage.client.dto;

import java.util.List;

/** Mirrors middleware's UsageItem - see its javadoc for why this no longer has flag/allowance/
 *  usage/remaining as flat fields (the real numbers live inside balances, per-category). */
public record UsageItem(
        String productCode,
        String productName,
        List<UsageBalance> balances
) {
}
