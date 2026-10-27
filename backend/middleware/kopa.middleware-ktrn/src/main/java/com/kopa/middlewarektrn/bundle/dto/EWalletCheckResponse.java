package com.kopa.middlewarektrn.bundle.dto;

/** RBSS-CCBS API_ACCESS s2.4.2 response - RESULT Y: can purchase / N: cannot, translated to a
 *  boolean so callers don't have to know KTRN's Y/N convention. */
public record EWalletCheckResponse(
        boolean canPurchase
) {
}
