package com.example.securityapi.utilities;

import java.util.*;

public final class SensitiveDataSanitizer {
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "pass", "pwd",
            "paymentInfo", "card", "cardNumber", "cc", "cvc", "cvv"
    );

    private static final String MASK = "****";

    private SensitiveDataSanitizer() {}

    /** Return a sanitized copy of a parameter map (for safe logging). */
    public static Map<String, String[]> maskParams(Map<String, String[]> in) {
        if (in == null || in.isEmpty()) return Map.of();
        Map<String, String[]> out = new LinkedHashMap<>(in.size());
        for (var e : in.entrySet()) {
            String k = e.getKey();
            String[] v = e.getValue();
            if (isSensitiveKey(k)) {
                out.put(k, v == null ? null : Arrays.stream(v).map(x -> MASK).toArray(String[]::new));
            } else {
                out.put(k, v);
            }
        }
        return out;
    }

    private static boolean isSensitiveKey(String key) {
        if (key == null) return false;
        String k = key.toLowerCase(Locale.ROOT);
        if (SENSITIVE_KEYS.contains(k)) return true;
        // catch common variations
        return k.contains("password") || k.contains("pass") || k.contains("card") || k.contains("cvv");
    }
}
