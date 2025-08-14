package com.example.securityapi.utilities;
public class CardValidator {
    private CardValidator() {
        // Utility class – prevent instantiation
    }
    public static boolean isValidCardNumber(String number) {
        number = number.replaceAll("\\s+", "");
        if (!number.matches("\\d{13,19}")) return false;
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}