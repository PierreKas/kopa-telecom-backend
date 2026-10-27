package com.kopa.middlewarektrn.registration.dto;

/** RBSS-CCBS API_ACCESS s2.6.2 response. */
public record SearchCustomerResponse(
        String custId,
        String custNm
) {
}
