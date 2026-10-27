package com.kopa.customeridentity.registration.client.dto;

public record SubscribeCustomerRequest(
        String soId,
        String loginId,
        String orgId,
        String custTp,
        String custNm,
        String corpRegNo,
        String bizRegNo,
        String birthDay,
        String email,
        String phoneNum,
        String addrDtl
) {
}
