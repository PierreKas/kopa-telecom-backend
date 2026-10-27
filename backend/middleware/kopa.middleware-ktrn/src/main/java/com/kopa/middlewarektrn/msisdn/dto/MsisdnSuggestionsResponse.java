package com.kopa.middlewarektrn.msisdn.dto;

import java.util.List;

/** RBSS-CCBS API_ACCESS s2.7.2 response - KTRN packs the 5 numbers as one comma-separated
 *  string, split here into a list. */
public record MsisdnSuggestionsResponse(
        List<String> msisdns
) {
}
