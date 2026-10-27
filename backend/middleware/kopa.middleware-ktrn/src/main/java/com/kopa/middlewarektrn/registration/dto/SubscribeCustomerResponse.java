package com.kopa.middlewarektrn.registration.dto;

/** RBSS-CCBS API_ACCESS s2.6.1 response. */
public record SubscribeCustomerResponse(
        String custId,
        String custNm,
        String birthDay,
        String addrDtl,
        String email,
        String phoneNum
) {
}
