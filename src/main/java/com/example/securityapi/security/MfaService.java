package com.example.securityapi.security;

import com.example.securityapi.model.Customer;
import com.example.securityapi.repository.CustomerRepository;
import com.example.securityapi.utilities.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class MfaService {

    private static final Logger log = LoggerFactory.getLogger(MfaService.class);
    // CSPRNG for OTP codes (000000-999999)
    private static final SecureRandom RNG = new SecureRandom();

    private final JavaMailSender mailSender;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Value("${app.mfa.code.ttl-seconds:300}")
    private long ttlSeconds;

    @Value("${app.mail.from:}")
    private String from;

    public MfaService(JavaMailSender mailSender,
                      CustomerRepository customerRepository,
                      PasswordEncoder passwordEncoder,
                      Clock clock) {
        this.mailSender = mailSender;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    /** Generate + persist one-time code and attempt to email it. */
    public void initiateMfa(Customer customer) {
        if (customer == null) return;

        // Generate a 6-digit code (zero-padded)
        String code = String.format("%06d", RNG.nextInt(1_000_000));
        String hash = passwordEncoder.encode(code);

        customer.setMfaCodeHash(hash);
        customer.setMfaCodeExpiry(LocalDateTime.now(clock).plusSeconds(ttlSeconds));
        customerRepository.save(customer);

        // Best-effort send: do not fail auth flow if email sending fails
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            log.warn("MFA email not sent: missing email for username={}", LogSanitizer.s(customer.getUsername()));
            return;
        }

        try {
            sendCode(customer.getEmail(), code);
        } catch (MailException ex) {
            // Keep code active; user can use /mfa/resend
            log.error("Failed to send MFA email to {}: {}", LogSanitizer.s(customer.getEmail()), ex.getMessage());
        }
    }

    /** Verify user-supplied code against stored hash + expiry (one-time). */
    public boolean verify(Customer customer, String code) {
        if (customer == null || code == null) return false;
        if (customer.getMfaCodeHash() == null || customer.getMfaCodeExpiry() == null) return false;

        // Expiry check (UTC)
        if (LocalDateTime.now(clock).isAfter(customer.getMfaCodeExpiry())) return false;

        boolean ok = passwordEncoder.matches(code, customer.getMfaCodeHash());
        if (ok) {
            // One-time use: clear on success
            customer.setMfaCodeHash(null);
            customer.setMfaCodeExpiry(null);
            customerRepository.save(customer);
        }
        return ok;
    }

    /** Resend simply issues a new one-time code with fresh expiry. */
    public void resend(Customer customer) {
        initiateMfa(customer);
    }

    private void sendCode(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        if (from != null && !from.isBlank()) {
            msg.setFrom(from);
        }
        msg.setTo(to);
        msg.setSubject("Your My Store verification code");
        long minutes = Math.max(1, ttlSeconds / 60);
        msg.setText("My store application\nYour verification code is: " + code + "\nIt expires in " + minutes + " minute(s).");
        mailSender.send(msg);
        if (log.isDebugEnabled()) {
            log.debug("MFA email queued to {}", LogSanitizer.s(to));
        }
    }
}
