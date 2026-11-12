package com.kopa.notifications.otp.dto;

public enum OtpPurpose {
    PIN_RESET,
    TRANSACTION_AUTH,
    /** The random, system-generated initial PIN sent after admin-driven SIM activation
     *  (2026-11-03) - unlike PIN_RESET/TRANSACTION_AUTH this is the PIN itself, not a one-time
     *  verification code, and carries its own (15-minute) validity wording in the SMS text. */
    INITIAL_PIN
}
