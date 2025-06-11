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
        if (integrityEnabled && !isValidCardNumber(paymentInfo)) {
            redirectAttributes.addFlashAttribute("error", "Invalid card number.");
            return "redirect:/cart";
        }

        double totalPaid = 0;
        for (CartItem item : cartItems) {
            cartItemService.updateQuantity(item.getId(), item.getQuantity());

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
    @GetMapping("/checkout-popup")
    public String checkoutPopup(HttpSession session, Model model) {
        Double total = (Double) session.getAttribute("checkoutTotal");
        model.addAttribute("totalPaid", total != null ? total : 0);
        return "checkout";  // returns checkout.html from templates/
    }
}
