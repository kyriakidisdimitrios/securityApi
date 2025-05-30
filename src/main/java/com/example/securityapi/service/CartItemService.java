// CartItemService.java
package com.example.securityapi.service;

import com.example.securityapi.model.Book;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.Customer;
import com.example.securityapi.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    public CartItemService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> getCartItems(Customer customer) {
        return cartItemRepository.findByCustomer(customer);
    }

    public void addToCart(Customer customer, Optional<Book> bookOpt, int quantity) {
        Book book = bookOpt.orElseThrow(() -> new IllegalArgumentException("Book not found"));

        CartItem item = CartItem.builder()
                .customer(customer)
                .book(book)
                .quantity(quantity)
                .build();

        cartItemRepository.save(item);
    }


    public void removeFromCart(Customer customer, Long bookId) {
        cartItemRepository.deleteByCustomerAndBookId(customer, bookId);
    }

    public void clearCart(Customer customer) {
        List<CartItem> items = cartItemRepository.findByCustomer(customer);
        cartItemRepository.deleteAll(items);
    }
}
