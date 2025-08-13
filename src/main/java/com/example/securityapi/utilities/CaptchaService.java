package com.example.securityapi.utilities;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

@Service
public class CaptchaService {
    private static final String CAPTCHA_SESSION_KEY = "captcha";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a CAPTCHA string and stores it in the session.
     * Returns the code so the controller/view can display it.
     */
    public String generateCaptcha(HttpSession session) {
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            captcha.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        String captchaStr = captcha.toString();
        session.setAttribute(CAPTCHA_SESSION_KEY, captchaStr);
        return captchaStr;
    }

    /**
     * Validates the user's input against the stored CAPTCHA.
     */
    public boolean validateCaptcha(String userInput, HttpSession session) {
        String captcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
        return captcha != null && captcha.equalsIgnoreCase(userInput);
    }

    /**
     * OPTIONAL: Returns a CAPTCHA image as a BufferedImage for <img> rendering.
     */
    public BufferedImage generateCaptchaImage(HttpSession session) {
        String captchaStr = generateCaptcha(session);
        int width = 150;
        int height = 50;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Text style
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.setColor(Color.BLACK);
        g.drawString(captchaStr, 20, 35);

        // Noise lines
        g.setColor(Color.GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();
        return image;
    }
}