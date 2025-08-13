package com.example.securityapi.repository;

import com.example.securityapi.model.CartHistory;
import com.example.securityapi.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartHistoryRepository extends JpaRepository<CartHistory, Long> {
    List<CartHistory> findAllByOrderByTimestampDesc();
    List<CartHistory> findByCustomerOrderByTimestampDesc(Customer customer);
}