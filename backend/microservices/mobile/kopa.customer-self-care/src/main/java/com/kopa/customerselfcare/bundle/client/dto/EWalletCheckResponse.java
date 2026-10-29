package com.kopa.customerselfcare.bundle.client.dto;

public record EWalletCheckResponse(boolean canPurchase, String msgcode, String message) {
}
