package com.example.securityapi.service;

import com.example.securityapi.model.ChartHistory;
import com.example.securityapi.repository.ChartHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChartHistoryService {

    private final ChartHistoryRepository chartHistoryRepository;

    public ChartHistoryService(ChartHistoryRepository chartHistoryRepository) {
        this.chartHistoryRepository = chartHistoryRepository;
    }

    public void saveChart(ChartHistory chartHistory) {
        chartHistoryRepository.save(chartHistory);
    }

    public List<ChartHistory> getAllChartsOrdered() {
        return chartHistoryRepository.findAllByOrderByTimestampDesc();
    }

    public ChartHistory getChartById(Long id) {
        return chartHistoryRepository.findById(id).orElse(null);
    }

    public void deleteChart(Long id) {
        chartHistoryRepository.deleteById(id);
    }
}