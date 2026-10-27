package com.kopa.middlewarektrn.registration.dto;

/** RBSS-CCBS API_ACCESS s2.1.2 response. */
public record SearchMsisdnByIccidResponse(
        String soId,
        String soNm,
        String msisdn
) {
}
