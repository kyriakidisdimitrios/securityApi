package com.example.securityapi.service;

import com.example.securityapi.advice.GlobalExceptionHandler;
import com.example.securityapi.model.Customer;
import com.example.securityapi.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import static com.example.securityapi.utilities.LogSanitizer.s;
import static com.example.securityapi.utilities.UrlValidatorUtil.isSafeUrl;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final CustomerRepository customerRepository;

    // ✅ Secure encoder instance
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ✅ Reusable RestTemplate instance
    private final RestTemplate restTemplate = new RestTemplate();

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public void saveCustomer(Customer customer) {
        // Check if the password is already hashed to avoid rehashing
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
        boolean match = passwordEncoder.matches(rawPassword, customer.getPassword());
        return match;
    }

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }

    public Customer findByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    /**
     * Handles safe post-login redirects to prevent Open Redirect vulnerabilities.
     */
    public String postLogin(String returnUrl) {
        if (returnUrl != null && isSafeUrl(returnUrl)) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/";
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
}
