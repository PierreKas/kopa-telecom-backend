package com.kopa.customeridentity.registration.client.dto;

public record NidaVerificationRequest(String nationalId, String photoBase64) {
}
