package com.example.securityapi.controller;

import com.example.securityapi.model.Book;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.Customer;
import com.example.securityapi.service.BookService;
import com.example.securityapi.service.CartItemService;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartItemService cartItemService;
    private final CustomerService customerService;
    private final BookService bookService;

    public CartController(CartItemService cartItemService,
                          CustomerService customerService,
                          BookService bookService) {
        this.cartItemService = cartItemService;
        this.customerService = customerService;
        this.bookService = bookService;
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer customer = customerService.findByUsername(username);
        List<CartItem> cartItems = cartItemService.getCartItems(customer);

        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getBook().getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("bookId") Long bookId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer customer = customerService.findByUsername(username);
        Optional<Book> book = bookService.getBookById(bookId);

        cartItemService.addToCart(customer, book, quantity);

        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("bookId") Long bookId,
                                 HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer customer = customerService.findByUsername(username);
        cartItemService.removeFromCart(customer, bookId);

        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam("paymentInfo") String paymentInfo,
                           HttpSession session,
                           Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer customer = customerService.findByUsername(username);

        List<CartItem> cartItems = cartItemService.getCartItems(customer);
        if (cartItems.isEmpty()) {
            model.addAttribute("error", "Your cart is empty!");
            return "cart";
        }

        // Simulate processing payment
        model.addAttribute("paymentSuccess", true);
        model.addAttribute("totalPaid", cartItems.stream()
                .mapToDouble(item -> item.getBook().getPrice() * item.getQuantity())
                .sum());

        cartItemService.clearCart(customer);

        return "checkout"; // Should correspond to a checkout.html template
    }
}
