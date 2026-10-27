package com.kopa.customerselfcare.balance.client.dto;

import java.util.List;

/** Mirrors kopa.middleware-ktrn's BalanceSummaryResponse. */
public record MiddlewareBalanceSummaryResponse(
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
        List<BundleHistoryEntryDto> bundleHistory
) {
}
