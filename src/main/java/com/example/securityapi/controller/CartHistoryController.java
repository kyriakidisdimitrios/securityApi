package com.example.securityapi.controller;


import com.example.securityapi.model.CartHistory;
import com.example.securityapi.model.Customer;
import com.example.securityapi.service.CartHistoryService;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

// History
@Controller
@RequestMapping("/history")
public class CartHistoryController {

    private final CartHistoryService cartHistoryService;
    private final CustomerService customerService;

    public CartHistoryController(CartHistoryService cartHistoryService, CustomerService customerService) {
        this.cartHistoryService = cartHistoryService;
        this.customerService = customerService;
    }

    @GetMapping
    public String viewPurchaseHistory(Model model, HttpSession session, Principal principal) {
        String username = (principal != null ? principal.getName()
                : (String) session.getAttribute("loggedInUser"));
        if (username == null) return "redirect:/login";
        Customer customer = customerService.findByUsername(username);
        List<CartHistory> userHistory = cartHistoryService.getChartsForCustomer(customer);
        model.addAttribute("historyList", userHistory);
        return "chart_history";
    }

}