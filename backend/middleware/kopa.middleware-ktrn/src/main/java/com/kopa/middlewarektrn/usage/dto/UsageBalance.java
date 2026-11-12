package com.kopa.middlewarektrn.usage.dto;

/** One entry from a PURCHASED_LIST record's trailing brace-wrapped ALL_BAL_LIST array (RBSS-CCBS
 *  v2.0 s2.1.2): "BAL_CATG,BAL_TYPE,ALLOWANCE,USAGE,REMAINING,BAL_EFF_DATETIME,BAL_EXP_DATETIME".
 *  category is DATA/SMS/VOICE per the doc - VOICE's unit is seconds, DATA/SMS are MB (live-
 *  confirmed: a purchased "Daily 1GB Bundle" reported allowance/remaining of exactly 1024). */
public record UsageBalance(
        String category,
        String allowance,
        String usage,
        String remaining
) {
}
