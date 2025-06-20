//package com.example.securityapi;
//
//import com.example.securityapi.model.Book;
//import com.example.securityapi.model.CartItem;
//import com.example.securityapi.model.Customer;
//import com.example.securityapi.repository.BookRepository;
//import com.example.securityapi.repository.CartItemRepository;
//import com.example.securityapi.repository.CustomerRepository;
//import com.example.securityapi.service.CartItemService;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class CartItemServiceTests {
//
//    @Autowired private CartItemService cartItemService;
//    @Autowired private CartItemRepository cartItemRepository;
//    @Autowired private CustomerRepository customerRepository;
//    @Autowired private BookRepository bookRepository;
//
//    @Test
//    void testTotalQuantityForCustomer() {
//        // --- Arrange ---
//        Customer customer = Customer.builder()
//                .username("cart_test_" + System.currentTimeMillis())
//                .password("pass")
//                .name("Cart")
//                .surname("Tester")
//                .email("cart_" + System.currentTimeMillis() + "@example.com")
//                .address("Athens")
//                .phoneNumber("1234567890")
//                .dateOfBirth(LocalDate.of(1990, 1, 1))
//                .isAdmin(false)
//                .build();
//        customer = customerRepository.save(customer);
//
//        Book book1 = new Book();
//        book1.setTitle("Book A");
//        book1.setPrice(10.0);
//        book1.setCopies(100);
//        book1 = bookRepository.save(book1);
//
//        Book book2 = new Book();
//        book2.setTitle("Book B");
//        book2.setPrice(15.0);
//        book2.setCopies(50);
//        book2 = bookRepository.save(book2);
//
//        CartItem item1 = new CartItem();
//        item1.setCustomer(customer);
//        item1.setBook(book1);
//        item1.setQuantity(2);
//
//        CartItem item2 = new CartItem();
//        item2.setCustomer(customer);
//        item2.setBook(book2);
//        item2.setQuantity(3);
//
//        cartItemRepository.save(item1);
//        cartItemRepository.save(item2);
//
//        // --- Act ---
//        int totalQuantity = cartItemService.getTotalQuantityForCustomer(customer);
//
//        // --- Assert ---
//        assertEquals(5, totalQuantity);
//    }
//}
