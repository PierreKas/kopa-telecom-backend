package com.kopa.middlewarektrn.identity.dto;

/** RBSS-CCBS API_ACCESS s2.5.3 response. result: Y/N. */
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
