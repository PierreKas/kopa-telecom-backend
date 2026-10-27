package com.kopa.middlewarektrn.activation.dto;

/** RBSS-CCBS API_ACCESS s2.5.2 response. */
public record SimpleActivationResponse(
        String result,
        String prodRate
) {
}
