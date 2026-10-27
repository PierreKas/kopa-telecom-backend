package com.kopa.customeridentity.registration.dto;

/** Deliberately drops the raw photo/signature base64 payloads the KTRN response carries - a
 *  mobile client doesn't need them echoed back, and there's no reason to push biometric data
 *  further than it needs to travel. */
public record VerifyNidaResponseDto(
        boolean verified,
        String name,
        String birthday,
        String gender
) {
}
