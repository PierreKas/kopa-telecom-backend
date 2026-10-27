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

    public static String otp(String code) {
        return "Your Kopa verification code is " + code + ". Valid for 5 minutes. Do not share it with anyone.";
    }

    public static String purchaseConfirmation(String productCode, String amount) {
        return "Kopa: your purchase of " + productCode + " (" + amount + " RWF) was successful. Thank you for using Kopa.";
    }

    public static String rechargeConfirmation(String amount) {
        return "Kopa: your recharge of " + amount + " RWF was successful. Thank you for using Kopa.";
    }

    public static String transactionFailed(String transactionType) {
        return "Kopa: your " + transactionType.toLowerCase().replace('_', ' ')
                + " could not be completed. Please try again or contact support.";
    }

    public static String activationConfirmation(String msisdn) {
        return "Welcome to Kopa! Your line " + msisdn + " is now active. Dial *XXX# or open the app to get started.";
    }
}
