package com.kopa.customeridentity.config;

public final class Consts {

    public static final String INTERNAL_SERVER_ERROR_MESSAGE =
            "An error occurred while completing this request. Please contact the server administrator.";
    public static final String INVALID_TOKEN_MESSAGE = "Invalid or expired session.";
    public static final String BAD_CREDENTIALS_MESSAGE = "MSISDN or PIN is incorrect.";

    public static final int AUTH_TOKEN_VALIDITY_HOURS = 12;

    private Consts() {
    }
}
