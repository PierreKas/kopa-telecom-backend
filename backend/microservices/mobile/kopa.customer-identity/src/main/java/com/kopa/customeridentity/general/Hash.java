package com.kopa.customeridentity.general;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hash {

    private Hash() {
    }

    public static String generateAuthToken(String timestamp, String uniqueId) {
        try {
            String concatData = timestamp + uniqueId;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(concatData.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
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
