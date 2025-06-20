package com.example.securityapi.service;

import com.example.securityapi.model.Customer;
import com.example.securityapi.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    //@Transactional
    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }
    public Customer findByUsername(String username) {
        return customerRepository.findByUsername(username).orElse(null);
    }
//
//    public boolean authenticateCustomer(String name, String password) {
//        return customerRepository.findByNameAndPassword(name, password).isPresent();
//    }

    public boolean authenticateCustomer(String username, String password) {
        return customerRepository.findByUsernameAndPassword(username, password).isPresent();
    }
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }
}
