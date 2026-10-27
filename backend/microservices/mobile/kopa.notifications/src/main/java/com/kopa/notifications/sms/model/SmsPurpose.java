package com.kopa.notifications.sms.model;

public enum SmsPurpose {
    OTP_TRANSACTION,
    OTP_PIN_RESET,
    PURCHASE_CONFIRMATION,
    RECHARGE_CONFIRMATION,
    ACTIVATION_CONFIRMATION,
    /** Not in the original db-diagram.md purpose list - added here since "transaction.failed" ->
     *  "Failure SMS" (architecture-reference.md s8.1) needs its own tag, distinct from a success
     *  confirmation. */
    TRANSACTION_FAILED
}
