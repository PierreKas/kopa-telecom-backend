package com.kopa.customerselfcare.general;

/** Bearer-header parsing only - this service never issues a token (customer-identity does), it
 *  only reads the opaque SHA-256 hex value out of the Authorization header to look it up. */
public final class Hash {

    private Hash() {
    }

    public static boolean isValidBearerAuthToken(String token) {
        return token != null && token.startsWith("Bearer ");
    }

    public static String getBearerAuthenticationData(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return null;
        }
        if (bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return bearerToken;
    }
}
