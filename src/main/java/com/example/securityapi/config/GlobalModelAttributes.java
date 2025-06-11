package com.example.securityapi.config;

import com.example.securityapi.model.Customer;
import com.example.securityapi.service.CartItemService;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private CustomerService customerService;

    @ModelAttribute
    public void addCartQuantityToModel(HttpSession session, Model model) {
        Object sessionUser = session.getAttribute("loggedInUser");

        if (sessionUser instanceof String username) {
            Customer customer = customerService.findByUsername(username);
            if (customer != null) {
                int totalQuantity = cartItemService.getTotalQuantityForCustomer(customer);
                model.addAttribute("cartQuantity", totalQuantity);
                session.setAttribute("cartQuantity", totalQuantity); // Optional
            }
        }
    }
}
