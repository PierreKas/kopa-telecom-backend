package com.kopa.customeridentity.admin.dto;

public record AdminCreateCustomerResponse(
        String customerId,
        String msisdn,
        String ktrnCustId,
        String status
) {
}
