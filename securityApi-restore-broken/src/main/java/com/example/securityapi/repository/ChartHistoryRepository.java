package com.example.securityapi.repository;

import com.example.securityapi.model.ChartHistory;
import com.example.securityapi.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChartHistoryRepository extends JpaRepository<ChartHistory, Long> {
    List<ChartHistory> findAllByOrderByTimestampDesc();
    List<ChartHistory> findByCustomerOrderByTimestampDesc(Customer customer);
}