package com.example.securityapi.service;

import com.example.securityapi.model.Book;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.ChartHistory;
import com.example.securityapi.model.Customer; // Make sure this is imported
import com.example.securityapi.repository.ChartHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    // --- THIS IS THE NEW METHOD THAT FIXES THE ERROR ---
    public List<ChartHistory> getChartsForCustomer(Customer customer) {
        // It calls the new repository method we created in Step 1
        return chartHistoryRepository.findByCustomerOrderByTimestampDesc(customer);
    }

//    public void save(ChartHistory history) {
//        if (history != null) {
//            chartHistoryRepository.save(history);
//        }
//    }
    public void savePurchaseHistory(Customer customer, List<CartItem> items, double totalPaid) {
        if (items == null || items.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (CartItem item : items) {
            Book book = item.getBook();
            sb.append(String.format("Book: %s, Qty: %d, Price: %.2f €%n",
                    book.getTitle(), item.getQuantity(), book.getPrice()));
        }
        ChartHistory history = new ChartHistory();
        history.setCustomer(customer);
        history.setTimestamp(LocalDateTime.now());
        history.setChartType("Purchase"); // or any descriptive title
        history.setChartData(sb.toString());
        chartHistoryRepository.save(history);
    }
}