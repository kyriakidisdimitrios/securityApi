package com.example.securityapi.controller;

import com.example.securityapi.exception.BookNotFoundException;
import com.example.securityapi.exception.CartItemException;
import com.example.securityapi.model.Book;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.Customer;
import com.example.securityapi.service.BookService;
import com.example.securityapi.service.CartItemService;
import com.example.securityapi.service.ChartHistoryService;
import com.example.securityapi.service.CustomerService;
import com.example.securityapi.utilities.CardValidator;
import jakarta.persistence.Converts;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cart") // Maps all methods starting with /cart
public class CartController {

    private final CartItemService cartItemService;
    private final CustomerService customerService;
    private final BookService bookService;
    private final ChartHistoryService chartHistoryService;

    public CartController(CartItemService cartItemService,
                          CustomerService customerService,
                          BookService bookService,
                          ChartHistoryService chartHistoryService) {
        this.cartItemService = cartItemService;
        this.customerService = customerService;
        this.bookService = bookService;
        this.chartHistoryService = chartHistoryService;
    }

    @GetMapping // becomes /cart
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

    @PutMapping("/update-ajax")
    @ResponseBody
    public Map<String, Object> updateCartAjax(@RequestBody Map<String, String> payload, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        Map<String, Object> response = new HashMap<>();

        if (username == null) {
            response.put("success", false);
            response.put("message", "Not logged in");
            return response;
        }

        try {
            // ✅ safe parsing with defaults
            Long cartItemId = Long.valueOf(payload.getOrDefault("cartItemId", "-1"));
            int quantity = Integer.parseInt(payload.getOrDefault("quantity", "0"));

            if (cartItemId < 0) {
                response.put("success", false);
                response.put("message", "Invalid cart item ID");
                return response;
            }
            if (quantity < 1) {
                response.put("success", false);
                response.put("message", "Quantity must be at least 1.");
                return response;
            }

            Customer customer = customerService.findByUsername(username);

            // 🔐 IDOR-safe service call (scoped to owner)
            cartItemService.updateQuantityOwned(cartItemId, quantity, customer);

            response.put("success", true);
        } catch (NumberFormatException nfe) {
            response.put("success", false);
            response.put("message", "Invalid number format for cartItemId or quantity");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/add") // becomes /cart/add
    public String addToCart(@RequestParam("bookId") Long bookId,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) {
            return "redirect:/login";
        }
        try {
            Customer customer = customerService.findByUsername(username);
            cartItemService.addToCart(customer, bookId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Book added to cart successfully!");
        } catch (BookNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books";
        } catch (CartItemException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
        return "redirect:/cart";
    }
    @DeleteMapping("/remove-ajax")
    @ResponseBody
    public Map<String, Object> removeCartAjax(@RequestBody Map<String, String> payload, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        Map<String, Object> response = new HashMap<>();

        if (username == null) {
            response.put("success", false);
            response.put("message", "Not logged in");
            return response;
        }

        try {
            // ✅ safe parsing with default
            Long cartItemId = Long.valueOf(payload.getOrDefault("cartItemId", "-1"));
            if (cartItemId < 0) {
                response.put("success", false);
                response.put("message", "Invalid cart item ID");
                return response;
            }

            Customer customer = customerService.findByUsername(username);

            // 🔐 IDOR-safe service call (scoped to owner)
            cartItemService.removeCartItemOwned(cartItemId, customer);

            response.put("success", true);
        } catch (NumberFormatException nfe) {
            response.put("success", false);
            response.put("message", "Invalid number format for cartItemId");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam("paymentInfo") String paymentInfo,
                           @RequestParam(value = "checkCardIntegrity", required = false) String checkCardIntegrity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) throws BookNotFoundException {

        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer customer = customerService.findByUsername(username);
        List<CartItem> cartItems = cartItemService.getCartItems(customer);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty!");
            return "redirect:/cart";
        }

        boolean integrityEnabled = (checkCardIntegrity != null);
        if (integrityEnabled && !CardValidator.isValidCardNumber(paymentInfo)) {
            redirectAttributes.addFlashAttribute("error", "Invalid card number.");
            return "redirect:/cart";
        }

        double totalPaid = 0;
        for (CartItem item : cartItems) {
            // 🔐 IDOR-safe quantity update during checkout too
            cartItemService.updateQuantityOwned(item.getId(), item.getQuantity(), customer);

            Book book = item.getBook();
            int remaining = book.getCopies() - item.getQuantity();
            book.setCopies(Math.max(remaining, 0));
            totalPaid += book.getPrice() * item.getQuantity();

            bookService.saveBook(book); // save in both cases
        }

        chartHistoryService.savePurchaseHistory(customer, cartItems, totalPaid);

        cartItemService.clearCart(customer);
        session.setAttribute("checkoutTotal", totalPaid);
        return "redirect:/cart/checkout-popup";
    }

    /*
    POST /cart/checkout
    → return "redirect:/cart/checkout-popup"
    → browser navigates to /cart/checkout-popup
    → @GetMapping("/checkout-popup") is invoked
    → return "checkout"
    → renders checkout.html
    */

    @GetMapping("/checkout-popup")
    public String checkoutPopup(HttpSession session, Model model) {
        Double total = (Double) session.getAttribute("checkoutTotal");
        model.addAttribute("totalPaid", total != null ? total : 0);
        return "checkout";  // returns checkout.html from templates/
    }
}
