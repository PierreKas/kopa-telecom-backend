package com.kopa.middlewarektrn.bundle.dto;

/** RBSS-CCBS API_ACCESS s2.4.2 response - RESULT Y: can purchase / N: cannot, translated to a
 *  boolean so callers don't have to know KTRN's Y/N convention. msgcode/message are passed
 *  through so the caller can tell "can't afford this" apart from an unrelated KTRN failure (e.g.
 *  an unrecognized PROD_CD) - same passthrough convention as BalanceLookupResponse. */
public record EWalletCheckResponse(
        boolean canPurchase,
        String msgcode,
        String message
) {
}
