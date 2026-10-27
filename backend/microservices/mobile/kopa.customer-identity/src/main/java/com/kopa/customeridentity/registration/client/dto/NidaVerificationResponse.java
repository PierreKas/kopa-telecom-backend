package com.kopa.customeridentity.registration.client.dto;

public record NidaVerificationResponse(
        String result,
        String msg,
        String photoBase64,
        String signatureBase64,
        String nationalId,
        String name,
        String birthday,
        String gender
) {
}
