package com.example.securityapi.config;
import com.example.securityapi.model.Customer;
import com.example.securityapi.service.CartItemService;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
//Inject dynamic model data globally into all views without repeating logic in every controller.
//Triggered on every request handled by a controller,
//
//It is triggered before the controller method executes
//
//Only active for controllers in the same component scan (usually your main app package)
@ControllerAdvice
public class GlobalModelAttributes {
    private final CartItemService cartItemService;
    private final CustomerService customerService;
    public GlobalModelAttributes(CartItemService cartItemService, CustomerService customerService) {
        this.cartItemService = cartItemService;
        this.customerService = customerService;
    }
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
