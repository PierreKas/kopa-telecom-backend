package com.kopa.middlewarektrn.registration.dto;

/** RBSS-CCBS API_ACCESS s2.6.1 response. msgcode/message are passed through so the caller can
 *  surface KTRN's real rejection reason instead of a generic "no CUST_ID" message - same
 *  passthrough convention as BalanceLookupResponse. */
public record SubscribeCustomerResponse(
        String custId,
        String custNm,
        String birthDay,
        String addrDtl,
        String email,
        String phoneNum,
        String msgcode,
        String message
) {
}
