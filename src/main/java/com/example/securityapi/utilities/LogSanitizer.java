package com.example.securityapi.utilities;

/** Neutralize CR/LF/tab and overly long user-controlled strings before logging. */
public final class LogSanitizer {
    private static final int MAX = 200;

    private LogSanitizer() {}

    public static String s(Object o) {
        if (o == null) return "null";
        String str = String.valueOf(o)
                .replace('\r', '_')
                .replace('\n', '_')
                .replace('\t', ' ');
        return str.length() > MAX ? str.substring(0, MAX) + "…" : str;
    }
}
