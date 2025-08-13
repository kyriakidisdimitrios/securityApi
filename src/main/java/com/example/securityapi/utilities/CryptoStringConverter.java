package com.example.securityapi.utilities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Converter(autoApply = false)
public class CryptoStringConverter implements AttributeConverter<String, String> {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;
    private static final SecureRandom RNG = new SecureRandom();
    private static final SecretKeySpec KEY = loadKey();
    private static SecretKeySpec loadKey() {
        try {
            String b64 = System.getenv("APP_DATA_KEY");
            if (b64 == null || b64.isBlank()) return null;
            byte[] key = Base64.getDecoder().decode(b64);
            int len = key.length;
            if (len != 16 && len != 24 && len != 32) return null;
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) { return null; }
    }
    private static boolean enabled() { return KEY != null; }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || !enabled()) return attribute;
        try {
            byte[] iv = new byte[IV_LEN]; RNG.nextBytes(iv);
            Cipher c = Cipher.getInstance(TRANSFORMATION);
            c.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = c.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) { return attribute; } // fail-safe: don’t break saves
    }
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || !enabled()) return dbData;
        try {
            byte[] all = Base64.getDecoder().decode(dbData);
            if (all.length <= IV_LEN) return dbData;
            byte[] iv = new byte[IV_LEN];
            byte[] ct = new byte[all.length - IV_LEN];
            System.arraycopy(all, 0, iv, 0, IV_LEN);
            System.arraycopy(all, IV_LEN, ct, 0, ct.length);
            Cipher c = Cipher.getInstance(TRANSFORMATION);
            c.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(c.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) { return dbData; } // fail-safe for legacy rows
    }
}
