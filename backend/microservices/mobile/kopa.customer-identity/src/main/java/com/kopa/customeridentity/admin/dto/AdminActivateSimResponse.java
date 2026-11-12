package com.kopa.customeridentity.admin.dto;

public record AdminActivateSimResponse(
        String customerId,
        String msisdn,
        String status
) {
}
