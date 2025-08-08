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
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final CustomerRepository customerRepository;

    // ✅ Secure encoder instance
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public void saveCustomer(Customer customer) {
        // Check if password is already hashed to avoid rehashing
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
            logger.warn("Login failed: user not found {}", username);
            return false;
        }
        // Debug print: hashed version of "admin"
        //System.out.println("Hash of 'admin' = " + passwordEncoder.encode("admin"));
        //logger.info("Comparing raw password '{}' with stored hash '{}'", rawPassword, customer.getPassword());
        boolean match = passwordEncoder.matches(rawPassword, customer.getPassword());
        //logger.info("Password match result: {}", match);
        return match;
    }


    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }

    public Customer findByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

}
