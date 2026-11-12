package com.kopa.customeridentity.registration.client.dto;

public record AdminActivateResponse(
        boolean success,
        String custId,
        String msgcode,
        String message
) {
}
