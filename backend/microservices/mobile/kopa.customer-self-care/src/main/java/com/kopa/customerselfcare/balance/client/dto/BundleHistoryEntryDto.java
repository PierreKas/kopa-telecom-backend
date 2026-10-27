package com.kopa.customerselfcare.balance.client.dto;

/** Mirrors kopa.middleware-ktrn's BundleHistoryEntry (one BUNDLE_HIST record from HP00005/HPOP101). */
public record BundleHistoryEntryDto(
        String action,
        String dateTime,
        String productName,
        String amount,
        String expiryDateTime,
        String flag
) {
}
