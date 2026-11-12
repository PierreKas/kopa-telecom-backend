package com.kopa.customerselfcare.transaction.model;

public enum TransactionStatus {
    INITIATED,
    OTP_PENDING,
    SUBMITTED,
    SUCCESS,
    FAILED,
    /** OTP window (Transaction.otpExpiresAt) lapsed without a successful verify - distinct from
     *  FAILED (which means a verify was attempted and the downstream call rejected it). */
    EXPIRED
}
