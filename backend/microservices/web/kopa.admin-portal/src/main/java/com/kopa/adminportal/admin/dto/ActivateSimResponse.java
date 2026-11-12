package com.kopa.adminportal.admin.dto;

public record ActivateSimResponse(
        String customerId,
        String msisdn,
        String status
) {
}
