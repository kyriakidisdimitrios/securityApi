package com.example.securityapi.service;

import com.example.securityapi.model.Book;
import com.example.securityapi.model.CartHistory;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.Customer; // Make sure this is imported
import com.example.securityapi.repository.CartHistoryRepository;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
@Service
public class CartHistoryService {

    private final CartHistoryRepository cartHistoryRepository;
    public CartHistoryService(CartHistoryRepository cartHistoryRepository) {
        this.cartHistoryRepository = cartHistoryRepository;
    }

    public void saveChart(CartHistory chartHistory) {
        cartHistoryRepository.save(chartHistory);
    }

    public List<CartHistory> getAllChartsOrdered() {
        return cartHistoryRepository.findAllByOrderByTimestampDesc();
    }

    public CartHistory getChartById(Long id) {
        return cartHistoryRepository.findById(id).orElse(null);
    }

    public void deleteChart(Long id) {
        cartHistoryRepository.deleteById(id);
    }

    // --- THIS IS THE NEW METHOD THAT FIXES THE ERROR ---
    public List<CartHistory> getChartsForCustomer(Customer customer) {
        // It calls the new repository method we created in Step 1
        return cartHistoryRepository.findByCustomerOrderByTimestampDesc(customer);
    }

    public void savePurchaseHistory(Customer customer, List<CartItem> items, double ignoredTotalPaid) {
        if (items == null || items.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (CartItem item : items) {
            Book book = item.getBook();
            sb.append(String.format("Book: %s, Qty: %d, Price: %.2f €%n",
                    book.getTitle(), item.getQuantity(), book.getPrice()));
        }
        CartHistory history = new CartHistory();
        history.setCustomer(customer);
        history.setTimestamp(LocalDateTime.now());
        history.setChartType("Purchase"); // or any descriptive title
        history.setChartData(sb.toString());
        cartHistoryRepository.save(history);
    }
}