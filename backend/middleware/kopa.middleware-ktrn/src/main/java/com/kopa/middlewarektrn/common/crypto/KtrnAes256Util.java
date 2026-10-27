package com.kopa.middlewarektrn.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 encryption for KTRN's CORP_REG_NO field (RBSS-CCBS API_ACCESS s2.6.1/s2.6.2: "ENCRYPTED
 * WITH AES, 256BIT KEY: aes256-ktrn-key1"). The doc names a 16-character key string, which is only
 * 128 bits as raw UTF-8 bytes - not the 256 bits it claims - so the actual key used here is the
 * SHA-256 hash of that string, a standard way to deterministically derive a real 256-bit key from
 * an arbitrary-length passphrase. AES/CBC/PKCS5Padding with a random IV prepended to the
 * ciphertext, both Base64-encoded together.
 *
 * <p><b>This exact scheme (key derivation, mode, IV handling) is unconfirmed against the real
 * KTRN gateway</b> - the doc doesn't specify a mode or how the IV should travel. This is a
 * reasonable-faith implementation to be validated once the real sandbox is reachable, same as the
 * XML tag-name spelling was for feature 2.1.1.
 */
public final class KtrnAes256Util {

    private static final String DOC_KEY_LITERAL = "aes256-ktrn-key1";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH_BYTES = 16;

    private static final SecretKeySpec KEY = deriveKey();
    private static final SecureRandom RANDOM = new SecureRandom();

    private KtrnAes256Util() {
    }

    public static String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new IvParameterSpec(iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] ivAndCipherText = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, ivAndCipherText, 0, iv.length);
            System.arraycopy(cipherText, 0, ivAndCipherText, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(ivAndCipherText);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to AES-256 encrypt CORP_REG_NO", ex);
        }
    }

    private static SecretKeySpec deriveKey() {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(DOC_KEY_LITERAL.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to derive KTRN AES-256 key", ex);
        }
    }
}
