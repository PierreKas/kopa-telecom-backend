package com.kopa.customeridentity.registration.client.dto;

public record SimpleActivationRequest(
        String soId,
        String msisdn,
        String prodCd,
        String loginId,
        String orgId,
        String iccid,
        String custId
) {
}
