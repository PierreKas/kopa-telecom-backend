package com.kopa.middlewarektrn.common.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KtrnAes256UtilTest {

    @Test
    void encryptsToNonNullBase64AndNeverThrows() {
        String encrypted = KtrnAes256Util.encrypt("431DFDAS1212123");
        assertNotNull(encrypted);
        assertTrue(encrypted.length() > 0);
    }

    @Test
    void sameInputEncryptsDifferentlyEachTime() {
        // Random IV per call is deliberate - confirms it's actually being applied, not a fixed IV.
        String first = KtrnAes256Util.encrypt("431DFDAS1212123");
        String second = KtrnAes256Util.encrypt("431DFDAS1212123");
        assertNotEquals(first, second);
    }

    @Test
    void nullInputReturnsNull() {
        assertNull(KtrnAes256Util.encrypt(null));
    }
}
