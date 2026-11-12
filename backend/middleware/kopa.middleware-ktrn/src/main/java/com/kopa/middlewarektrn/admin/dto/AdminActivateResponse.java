package com.kopa.middlewarektrn.admin.dto;

public record AdminActivateResponse(
        boolean success,
        String custId,
        String msgcode,
        String message
) {
}
