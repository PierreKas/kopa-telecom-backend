package com.kopa.customeridentity.registration.client.dto;

public record PassportVerificationRequest(String docNumber, String issuedBy, String registerId) {
}
