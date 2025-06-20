package com.example.securityapi.repository;

import com.example.securityapi.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailAndPassword(String email, String password);
    Optional<Customer> findByNameAndPassword(String name, String password);
    Optional<Customer> findByUsernameAndPassword(String username, String password);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhoneNumber(String phoneNumber);
}
