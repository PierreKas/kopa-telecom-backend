package com.kopa.adminportal.config;

public final class Consts {

    public static final String INVALID_TOKEN_MESSAGE = "Invalid or expired session.";
    public static final String BAD_CREDENTIALS_MESSAGE = "Username or password is incorrect.";

    // Shorter than the Mobile side's 12 hours (kopa.customer-identity.config.Consts) - this is a
    // shared, browser-based, higher-privilege surface over customer data, so a stricter idle
    // timeout is warranted (FR-15).
    public static final int AUTH_TOKEN_VALIDITY_HOURS = 2;

    private Consts() {
    }
}
