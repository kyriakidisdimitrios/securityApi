// CartItemRepository.java
package com.example.securityapi.repository;

import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomer(Customer customer);
    void deleteByCustomerAndBookId(Customer customer, Long bookId);
}