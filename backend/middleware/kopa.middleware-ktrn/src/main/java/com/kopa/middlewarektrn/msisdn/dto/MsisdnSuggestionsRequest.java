package com.kopa.middlewarektrn.msisdn.dto;

import jakarta.validation.constraints.NotBlank;

/** RBSS-CCBS API_ACCESS s2.7.2 - Recommend 5 MSISDNs (HP00005/HPOP602). */
public record MsisdnSuggestionsRequest(
        @NotBlank String soId
) {
}
