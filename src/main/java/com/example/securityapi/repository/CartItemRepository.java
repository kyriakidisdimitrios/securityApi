package com.example.securityapi.repository;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.securityapi.model.Book;
import java.util.List;
import java.util.Optional;
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomer(Customer customer);
    void deleteByCustomerAndBookId(Customer customer, Long bookId);
    CartItem findByCustomerAndBook(Customer customer, Book book);
    List<CartItem> findByBookId(Long bookId);
    // 🔐 NEW: I.D.O.R-safe accessors - CWE-639
    Optional<CartItem> findByIdAndCustomer_Id(Long id, Long customerId);
    void deleteByIdAndCustomer_Id(Long id, Long customerId);
}