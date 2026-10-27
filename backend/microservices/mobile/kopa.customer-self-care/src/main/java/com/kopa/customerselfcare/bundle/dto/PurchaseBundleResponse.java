package com.kopa.customerselfcare.bundle.dto;

public record PurchaseBundleResponse(
        String transactionId,
        String status,
        String result
) {
}
