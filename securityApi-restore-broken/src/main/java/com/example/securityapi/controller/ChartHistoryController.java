package com.example.securityapi.controller;


import com.example.securityapi.model.Book;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.ChartHistory;
import com.example.securityapi.model.Customer;
import com.example.securityapi.service.ChartHistoryService;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

// History
@Controller
@RequestMapping("/history")
public class ChartHistoryController {

    private final ChartHistoryService chartHistoryService;
    private final CustomerService customerService;

    public ChartHistoryController(ChartHistoryService chartHistoryService, CustomerService customerService) {
        this.chartHistoryService = chartHistoryService;
        this.customerService = customerService;
    }

    @GetMapping
    public String viewPurchaseHistory(Model model, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) {
            return "redirect:/login";
        }

        Customer customer = customerService.findByUsername(username);
        // Use our new service method to get history for the logged-in user
        List<ChartHistory> userHistory = chartHistoryService.getChartsForCustomer(customer);

        model.addAttribute("historyList", userHistory);
        return "chart_history"; // The name of our new HTML file
    }


}