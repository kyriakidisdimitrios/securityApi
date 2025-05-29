package com.example.securityapi.repository;

import com.example.securityapi.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Find by exact title
    Optional<Book> findByTitle(String title);

    // Find all books by a given author
    List<Book> findByAuthor(String author);

    // Search books with titles containing a keyword (case-insensitive)
    List<Book> findByTitleContainingIgnoreCase(String keyword);
}
