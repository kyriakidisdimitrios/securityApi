// CartItemService.java
package com.example.securityapi.service;

import com.example.securityapi.exception.BookNotFoundException;
import com.example.securityapi.exception.CartItemException;
import com.example.securityapi.model.Book;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.Customer;
import com.example.securityapi.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final BookService bookService;
    public CartItemService(CartItemRepository cartItemRepository, BookService bookService) {
        this.cartItemRepository = cartItemRepository;
        this.bookService = bookService;
    }
    public List<CartItem> getCartItems(Customer customer) {
        return cartItemRepository.findByCustomer(customer);
    }

//    public void addToCart(Customer customer, Optional<Book> bookOpt, int quantity) {
//        Book book = bookOpt.orElseThrow(() -> new IllegalArgumentException("Book not found"));
//
//        CartItem item = CartItem.builder()
//                .customer(customer)
//                .book(book)
//                .quantity(quantity)
//                .build();
//
//        cartItemRepository.save(item);
//    }
//public void addToCart(Customer customer, Optional<Book> bookOpt, int quantity) {
//public void addToCart(Customer customer, Long bookId, int quantity) throws BookNotFoundException, CartItemException {
//    //Book book = bookOpt.orElseThrow(() -> new CartItemException("Cannot add to cart: Book not found."));
//    Book book = bookService.getBookById(bookId);
//    if (quantity <= 0) {
//        throw new CartItemException("Quantity must be a positive number.");
//    }
//    if (quantity > book.getCopies()) {
//        throw new CartItemException("Cannot add to cart. Requested quantity exceeds available stock.");
//    }
//    CartItem item = CartItem.builder()
//            .customer(customer)
//            .book(book)
//            .quantity(quantity)
//            .build();
//    cartItemRepository.save(item);
//}
    public void addToCart(Customer customer, Long bookId, int quantity) throws BookNotFoundException, CartItemException {
        Book book = bookService.getBookById(bookId);
        if (quantity <= 0) {
            throw new CartItemException("Quantity must be a positive number.");
        }
        // Check if item already exists in the cart
        CartItem existingItem = cartItemRepository.findByCustomerAndBook(customer, book);
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;

            if (newQuantity > book.getCopies()) {
                throw new CartItemException("Cannot add to cart. Total quantity exceeds available stock.");
            }

            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            if (quantity > book.getCopies()) {
                throw new CartItemException("Cannot add to cart. Requested quantity exceeds available stock.");
            }
            CartItem newItem = CartItem.builder()
                    .customer(customer)
                    .book(book)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newItem);
        }
    }
//    public void removeCartItemById(Long cartItemId) {
//        cartItemRepository.deleteById(cartItemId);
//    }
    public void removeCartItemById(Long cartItemId) throws CartItemException {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new CartItemException("Cannot remove item. Cart item with ID " + cartItemId + " not found.");
        }
        cartItemRepository.deleteById(cartItemId);
    }
    public void removeFromCart(Customer customer, Long bookId) {
        cartItemRepository.deleteByCustomerAndBookId(customer, bookId);
    }

    public void clearCart(Customer customer) {
        List<CartItem> items = cartItemRepository.findByCustomer(customer);
        cartItemRepository.deleteAll(items);
    }
//    public void updateQuantity(Long cartItemId, int quantity) {
//        CartItem cartItem = cartItemRepository.findById(cartItemId)
//                .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));
//
//        Book book = cartItem.getBook();
//        int availableCopies = book.getCopies();
//
//        if (quantity < 1) {
//            throw new IllegalArgumentException("Quantity must be at least 1.");
//        }
//
//        if (quantity > availableCopies) {
//            throw new IllegalArgumentException("Requested quantity exceeds available copies.");
//        }
//
//        cartItem.setQuantity(quantity);
//        cartItemRepository.save(cartItem);
//    }
    public void updateQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemException("Cannot update quantity. Cart item with ID " + cartItemId + " not found."));
        Book book = cartItem.getBook();
        int availableCopies = book.getCopies();

        if (quantity < 1) {
            throw new CartItemException("Quantity must be at least 1.");
        }

        if (quantity > availableCopies) {
            throw new CartItemException("Cannot update quantity. Requested quantity (" + quantity
                    + ") exceeds available copies (" + availableCopies + ").");
        }
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }
    public int getTotalQuantityForCustomer(Customer customer) { //Fetches all CartItem. Returns a List<CartItem>
        return cartItemRepository.findByCustomer(customer).stream()
                .mapToInt(CartItem::getQuantity)//Converts each CartItem object in the stream into its quantity value (int).
                                                //Result: IntStream of quantities like 2, 1, 3, ...
                .sum();
    }
}
