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
@RequestMapping("/cart") //Maps all methods starting with /cart
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

    @GetMapping //@GetMapping becomes /cart
    public String viewCart(Model model, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer customer = customerService.findByUsername(username);
        List<CartItem> cartItems = cartItemService.getCartItems(customer);

        double totalPrice = cartItems.stream()//Creates a stream from the cart items
                                              //Maps each item to a subtotal: price * quantity
                                              //Converts to a double stream and sums them up
                .mapToDouble(item -> item.getBook().getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

   // @PostMapping("/update")
   @PutMapping("/update-ajax") //updating
   @ResponseBody //Tells Spring not to render a view, but instead return the object (usually a Map or JSON) directly in the HTTP response body. Used for AJAX/REST responses.
   public Map<String, Object> updateCartAjax(@RequestBody Map<String, String> payload, HttpSession session) { //@RequestBody Maps the incoming JSON body of a POST/PUT/DELETE request to a Map<String, String> or custom object. Useful for AJAX (not form posts).
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

    @PostMapping("/add") //@PostMapping("/add") becomes /cart/add
    public String addToCart(@RequestParam("bookId") Long bookId,
                            //@RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) { // Add RedirectAttributes for user feedback
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) {
            return "redirect:/login";

        }
        try {
            Customer customer = customerService.findByUsername(username);
            cartItemService.addToCart(customer, bookId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Book added to cart successfully!"); ////This is used to pass flash messages (one-time attributes) during a redirect

        } catch (BookNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books"; //The prefix redirect: tells Spring not to render a template called cart.html, but instead send a client-side HTTP redirect to /cart.
            //This avoids double form submissions and follows the POST-Redirect-GET pattern.// Redirect to the main book list page

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
@DeleteMapping("/remove-ajax") //@DeleteMapping("/remove-ajax"): This is an endpoint for AJAX-based cart item removal (uses fetch or $.ajax)
@ResponseBody
public Map<String, Object> removeCartAjax(@RequestBody Map<String, String> payload, HttpSession session) { //@RequestBody Map<String, String> payload: Reads JSON payload like {"cartItemId": "123"}
    String username = (String) session.getAttribute("loggedInUser");
    Map<String, Object> response = new HashMap<>(); //Map<String, Object> response = new HashMap<>();: Will hold response data like:{"success": true, "message": "Removed successfully"}

    if (username == null) {
        response.put("success", false); //"Add a key "success" to the map with value true.
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
        //if (integrityEnabled && !isValidCardNumber(paymentInfo)) {
        if (integrityEnabled && !CardValidator.isValidCardNumber(paymentInfo)) {
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
        return "redirect:/cart/checkout-popup"; //(URL-based redirection) @GetMapping("/checkout-popup") -> return "checkout" (controller-based rendering)
    }
/*
POST /cart/checkout
→ return "redirect:/cart/checkout-popup"
→ browser navigates to /cart/checkout-popup
→ @GetMapping("/checkout-popup") is invoked
→ return "checkout"
→ renders checkout.html
 */

//    private boolean isValidCardNumber(String number) {
//        number = number.replaceAll("\\s+", "");
//        if (!number.matches("\\d{13,19}")) return false;
//
//        int sum = 0;
//        boolean alternate = false;
//        for (int i = number.length() - 1; i >= 0; i--) {
//            int n = Integer.parseInt(number.substring(i, i + 1));
//            if (alternate) {
//                n *= 2;
//                if (n > 9) n -= 9;
//            }
//            sum += n;
//            alternate = !alternate;
//        }
//        return (sum % 10 == 0);
//    }
    @GetMapping("/checkout-popup")
    public String checkoutPopup(HttpSession session, Model model) {
        Double total = (Double) session.getAttribute("checkoutTotal");
        model.addAttribute("totalPaid", total != null ? total : 0);
        return "checkout";  // returns checkout.html from templates/
    }
}
