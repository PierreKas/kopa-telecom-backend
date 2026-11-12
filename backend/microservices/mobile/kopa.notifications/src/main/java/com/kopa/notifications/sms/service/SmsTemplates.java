package com.kopa.notifications.sms.service;

/** Plain static text templates - no DB-backed template engine at this stage (see templateCode
 *  column on sms_messages, which just tags which of these was used). */
public final class SmsTemplates {

    private SmsTemplates() {
    }

    public static final String OTP = "OTP_V1";
    public static final String PURCHASE_CONFIRMATION = "PURCHASE_CONFIRMATION_V1";
    public static final String RECHARGE_CONFIRMATION = "RECHARGE_CONFIRMATION_V1";
    public static final String TRANSACTION_FAILED = "TRANSACTION_FAILED_V1";
    public static final String ACTIVATION_CONFIRMATION = "ACTIVATION_CONFIRMATION_V1";
    public static final String INITIAL_PIN = "INITIAL_PIN_V1";

    public static String otp(String fullName, String code) {
        return greeting(fullName) + "your verification code is " + code
                + ". Valid for 5 minutes. Do not share it with anyone.";
    }

    /** Sent right after admin-driven SIM activation (2026-11-03) - the admin never sets or sees
     *  this PIN; it's generated here and only ever shown to the customer, on their own phone. */
    public static String initialPin(String fullName, String code) {
        return greeting(fullName) + "your temporary PIN is " + code + ". Valid for 15 minutes - "
                + "use it to log in, then set a PIN of your own. Do not share it with anyone.";
    }

    public static String purchaseConfirmation(String fullName, String productCode, String amount) {
        return greeting(fullName) + "your purchase of " + productCode + " (" + amount
                + " RWF) was successful. Thank you for using Kopa.";
    }

    public static String rechargeConfirmation(String fullName, String amount) {
        return greeting(fullName) + "your recharge of " + amount + " RWF was successful. Thank you for using Kopa.";
    }

    /** detail is KTRN's own real rejection reason (e.g. "Not sufficient balance.") when one was
     *  captured - shown to the customer instead of a generic "could not be completed" with no
     *  explanation, same as every API error response already surfaces it. */
    public static String transactionFailed(String fullName, String transactionType, String detail) {
        String what = transactionType.toLowerCase().replace('_', ' ');
        String reason = (detail != null && !detail.isBlank()) ? ": " + detail : "";
        return greeting(fullName) + "your " + what + " could not be completed" + reason
                + ". Please try again or contact support.";
    }

    public static String activationConfirmation(String fullName, String msisdn) {
        return greeting(fullName) + "welcome to Kopa! Your line " + msisdn
                + " is now active. Dial *XXX# or open the app to get started.";
    }

    /** "Dear Customer {fullName}, " - every template appends the rest of the sentence after this.
     *  Falls back to a plain "Dear Customer, " when the name isn't known. */
    private static String greeting(String fullName) {
        return (fullName != null && !fullName.isBlank())
                ? "Dear Customer " + fullName + ", "
                : "Dear Customer, ";
    }
}
