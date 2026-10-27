package com.kopa.middlewarektrn.msisdn.dto;

/** RBSS-CCBS API_ACCESS s2.7.1 response - RESULT Y available / N not available, translated to a
 *  boolean so callers don't have to know KTRN's Y/N convention. */
public record MsisdnStatusResponse(
        boolean available
) {
}
