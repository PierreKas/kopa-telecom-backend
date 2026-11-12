package com.kopa.adminportal.admin.dto;

public record CreateCustomerResponse(
        String customerId,
        String msisdn,
        String ktrnCustId,
        String status
) {
}
