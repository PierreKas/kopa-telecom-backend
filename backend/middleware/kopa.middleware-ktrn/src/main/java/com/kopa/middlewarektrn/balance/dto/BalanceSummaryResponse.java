package com.kopa.middlewarektrn.balance.dto;

import java.util.List;

/** Raw KTRN outcome for HP00005/HPOP101 (RBSS-CCBS API_ACCESS s2.1.5), header and body fields
 *  flattened - same msgcode/message passthrough convention as BalanceLookupResponse. */
public record BalanceSummaryResponse(
        String msgcode,
        String message,
        String msisdn,
        String soId,
        String soNm,
        String remainingAmount,
        String totalVolume,
        String amountUsed,
        String todayVolume,
        String remainVolume,
        String prodNm,
        String prodGrp,
        String appUseYn,
        List<BundleHistoryEntry> bundleHistory
) {
}
