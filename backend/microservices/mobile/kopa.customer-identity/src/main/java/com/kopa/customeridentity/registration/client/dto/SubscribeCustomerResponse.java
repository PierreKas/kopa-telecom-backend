package com.kopa.customeridentity.registration.client.dto;

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
