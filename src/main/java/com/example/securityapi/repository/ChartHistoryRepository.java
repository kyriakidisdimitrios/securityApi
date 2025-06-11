package com.example.securityapi.repository;

import com.example.securityapi.model.ChartHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ChartHistoryRepository extends JpaRepository<ChartHistory, Long> {
    List<ChartHistory> findAllByOrderByTimestampDesc();
}