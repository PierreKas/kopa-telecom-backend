package com.kopa.middlewarektrn.balance.dto;

/** One ";"-separated BUNDLE_HIST record from HP00005/HPOP101 (RBSS-CCBS API_ACCESS s2.1.5) -
 *  action/dateTime/productName/amount/expiryDateTime/flag, comma-separated, in the doc's own
 *  order. Dates are left as the raw dd/MM/yyyy HH:mm:ss strings the gateway sends - no ISO
 *  format guaranteed, parsing is the calling microservice's call to make. */
public record BundleHistoryEntry(
        String action,
        String dateTime,
        String productName,
        String amount,
        String expiryDateTime,
        String flag
) {
}
