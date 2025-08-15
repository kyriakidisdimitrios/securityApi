package com.example.securityapi.utilities;

import java.util.Set;

/** Central password-strength checks for registration / password change. */
public final class PasswordPolicy {

    // Minimal denylist; extend if you want
    private static final Set<String> COMMON = Set.of(
            "password", "passw0rd", "123456", "123456789", "qwerty",
            "iloveyou", "admin", "letmein", "welcome", "abc123"
    );

    private PasswordPolicy() {}

    /**
     * ✅ FIXED: One-line requirement message for UI.
     * This now correctly states the 3-12 character length. This was the source of the incorrect error message.
     */
    public static String requirements() {
        return "Password must be 3–12 chars and include upper, lower, digit and symbol; "
                + "no obvious/common passwords or 3+ repeating characters.";
    }

    /**
     * True when password is strong enough.
     * This logic correctly enforces the 3-12 character length.
     */
    public static boolean isStrong(String pw, String username, String email) {
        if (pw == null) return false;
        String p = pw.trim();

        // Length (3-12 characters)
        if (p.length() < 3 || p.length() > 12) return false;

        // Char classes (must contain uppercase, lowercase, digit, and symbol)
        boolean up = false, lo = false, di = false, sp = false;
        for (int i = 0; i < p.length(); i++) {
            char c = p.charAt(i);
            if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) lo = true;
            else if (Character.isDigit(c)) di = true;
                // Any non-alphanumeric character is considered a special character for this check
            else if (!Character.isLetterOrDigit(c)) sp = true;
        }
        if (!(up && lo && di && sp)) return false;

        // Obvious repeats like "aaa" or "111"
        if (p.matches(".*(.)\\1{2,}.*")) return false;

        // Common passwords from the denylist
        if (COMMON.contains(p.toLowerCase())) return false;

        // Don’t contain username or email local-part
        if (username != null && !username.isBlank()
                && p.toLowerCase().contains(username.toLowerCase())) return false;

        if (email != null && email.contains("@")) {
            String local = email.substring(0, email.indexOf('@')).toLowerCase();
            if (!local.isBlank() && p.toLowerCase().contains(local)) return false;
        }

        // If all checks pass, the password is strong
        return true;
    }
}