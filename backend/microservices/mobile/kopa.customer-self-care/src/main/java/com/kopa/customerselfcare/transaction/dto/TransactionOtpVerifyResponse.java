package com.kopa.customerselfcare.transaction.dto;

public record TransactionOtpVerifyResponse(
        String transactionId,
        String status,
        String message
) {
}
