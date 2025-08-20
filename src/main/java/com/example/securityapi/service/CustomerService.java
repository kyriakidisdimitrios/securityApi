package com.example.securityapi.service;
import com.example.securityapi.model.Customer;
import com.example.securityapi.repository.CustomerRepository;
import com.example.securityapi.utilities.PasswordPolicy;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import static com.example.securityapi.utilities.LogSanitizer.s;
import static com.example.securityapi.utilities.PasswordPolicy.isStrong;
import static com.example.securityapi.utilities.UrlValidatorUtil.isSafeUrl;
import java.nio.charset.StandardCharsets;
import java.util.List;
@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;
    // ✅ Secure encoder instance
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    // ✅ Reusable RestTemplate instance
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    @Transactional
    public void saveCustomer(Customer customer) {
        // Enforce strong password (CWE-620)
        if (!isStrong(customer.getPassword(), customer.getUsername(), customer.getEmail())) {
            logger.warn("Weak password rejected for username={}", s(customer.getUsername()));
            throw new IllegalArgumentException(PasswordPolicy.requirements());
        }
        // Hash if not already hashed
        if (!customer.getPassword().startsWith("$2a$")) {
            String hashedPassword = passwordEncoder.encode(customer.getPassword());
            customer.setPassword(hashedPassword);
        }
        customerRepository.save(customer);
    }
    public Customer findByUsername(String username) {
        return customerRepository.findByUsername(username).orElse(null);
    }
    public boolean authenticateCustomer(String username, String rawPassword) {
        Customer customer = customerRepository.findByUsername(username).orElse(null);
        if (customer == null) {
            logger.warn("Login failed: user not found {}", s(username));
            return false;
        }
        return passwordEncoder.matches(rawPassword, customer.getPassword());
    }
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }
    public Customer findByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }
    /**
     * Safely fetches an avatar from a trusted external source after S.S.R.F validation.
     */
    @GetMapping("/profile/fetch-avatar")
    public String fetchAvatar(@RequestParam("url") String url, RedirectAttributes ra) {
        if (!isSafeUrl(url)) {
            return "redirect:/ssrf-blocked?reason=" +
                    UriUtils.encode("Invalid or unsafe URL", StandardCharsets.UTF_8) +
                    (url != null ? "&url=" + UriUtils.encode(url, StandardCharsets.UTF_8) : "");
        }
        // If you want to actually call the service:
        // customerService.fetchAvatar(url);
        ra.addFlashAttribute("successMessage", "Avatar fetch initiated (demo).");
        return "redirect:/";
    }
    @Transactional
    public void updateMfaEnabledByUsername(String username, boolean enabled) {
        Customer db = customerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        db.setMfaEnabled(enabled);
        if (!enabled) {
            db.setMfaCodeHash(null);
            db.setMfaCodeExpiry(null);
        }
        customerRepository.save(db); // no password policy touched
    }

    @Transactional
    public void persistMfaState(Customer customerWithNewMfaState) {
        Customer db = customerRepository.findById(customerWithNewMfaState.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: id=" + customerWithNewMfaState.getId()));

        db.setMfaEnabled(customerWithNewMfaState.isMfaEnabled());
        db.setMfaCodeHash(customerWithNewMfaState.getMfaCodeHash());
        db.setMfaCodeExpiry(customerWithNewMfaState.getMfaCodeExpiry());
        customerRepository.save(db);
    }
}
