package com.example.securityapi.controller;

import com.example.securityapi.model.Customer;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Make loggedInUser available to all views
    @ModelAttribute
    public void addLoggedInUserToModel(HttpSession session, Model model) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        model.addAttribute("loggedInUser", loggedInUser);
    }

    // Home page
    @GetMapping
    public String home() {
        return "index";
    }

    // Customer list page
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "customers";
        // TODO: Restrict this page to ADMIN only in the future with Spring Security
    }

    // Show registration form
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "register";
    }

    // Process registration
    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") Customer customer) {
        customerService.saveCustomer(customer);
        return "redirect:/login";
    }

    // Show login form
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("customer", new Customer()); // Needed for th:object
        return "login";
    }

    @PostMapping("/login")
    public String loginCustomer(@ModelAttribute("customer") Customer customer,
                                HttpServletRequest request,
                                Model model) {

        String username = customer.getUsername();
        String password = customer.getPassword();

        logger.info("Customer '{}' is attempting to log in", username);

        boolean authenticated = customerService.authenticateCustomer(username, password);

        if (authenticated) {
            request.getSession().invalidate();
            request.getSession(true).setAttribute("loggedInUser", username);
            logger.info("Customer '{}' logged in successfully", username);
            return "redirect:/";
        } else {
            model.addAttribute("customer", new Customer());
            model.addAttribute("error", "Invalid username or password!");
            return "login";
        }
    }

    @GetMapping("/customLogout")
    public String logout(HttpServletRequest request) {
        logger.info("Customer '{}' Logout");
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("Customer '{}' Logout", session.getAttribute("loggedInUser"));
            session.invalidate();
        }
        return "redirect:/login?logout";
    }



}
