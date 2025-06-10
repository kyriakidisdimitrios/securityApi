package com.example.securityapi.controller;

import com.example.securityapi.exception.BookNotFoundException;
import com.example.securityapi.exception.CartItemException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

   // @PostMapping("/update")
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
           Long cartItemId = Long.parseLong(payload.get("cartItemId"));
           int quantity = Integer.parseInt(payload.get("quantity"));
           cartItemService.updateQuantity(cartItemId, quantity);
           response.put("success", true);
       } catch (Exception e) {
           response.put("success", false);
           response.put("message", e.getMessage());
       }

       return response;
   }

    @PostMapping("/add")
    public String addToCart(@RequestParam("bookId") Long bookId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) { // Add RedirectAttributes for user feedback
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
            return "redirect:/books"; // Redirect to the main book list page

        } catch (CartItemException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart"; // Redirect back to the cart
        }
        return "redirect:/cart";
    }

//    @PostMapping("/remove")
//    public String removeFromCart(@RequestParam("cartItemId") Long cartItemId,
//                                 HttpSession session) {
//        String username = (String) session.getAttribute("loggedInUser");
//        if (username == null) return "redirect:/login";
//
//        cartItemService.removeCartItemById(cartItemId);
//        return "redirect:/cart";
//    }
    //@PostMapping("/remove")
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
        Long cartItemId = Long.parseLong(payload.get("cartItemId"));
        cartItemService.removeCartItemById(cartItemId);
        response.put("success", true);
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", e.getMessage());
    }

    return response;
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

        if (!isValidCardNumber(paymentInfo)) {
            model.addAttribute("error", "Invalid card number.");
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalPrice", cartItems.stream()
                    .mapToDouble(item -> item.getBook().getPrice() * item.getQuantity())
                    .sum());
            return "cart";
        }

        // Update the quantity in DB only on successful checkout
        for (CartItem item : cartItems) {
            cartItemService.updateQuantity(item.getId(), item.getQuantity());
        }

        model.addAttribute("paymentSuccess", true);
        model.addAttribute("totalPaid", cartItems.stream()
                .mapToDouble(item -> item.getBook().getPrice() * item.getQuantity())
                .sum());

        cartItemService.clearCart(customer);
        return "checkout";
    }

    private boolean isValidCardNumber(String number) {
        number = number.replaceAll("\\s+", "");
        if (!number.matches("\\d{13,19}")) return false;

        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
