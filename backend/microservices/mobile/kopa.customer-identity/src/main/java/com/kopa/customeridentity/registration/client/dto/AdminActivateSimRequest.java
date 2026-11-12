package com.kopa.customeridentity.registration.client.dto;

public record AdminActivateSimRequest(
        String msisdn,
        String iccid,
        String custId
) {
}
