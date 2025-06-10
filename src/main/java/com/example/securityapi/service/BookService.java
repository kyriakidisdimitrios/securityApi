package com.example.securityapi.service;

import com.example.securityapi.exception.BookNotFoundException;
import com.example.securityapi.model.Book;
import com.example.securityapi.repository.BookRepository;
import org.springframework.stereotype.Service;
import com.example.securityapi.service.BookService;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

//    public Optional<Book> getBookById(Long id) {
//        return bookRepository.findById(id);
//    }

    public Book getBookById(Long id) throws BookNotFoundException {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

//    public Book updateBook(Long id, Book updatedBook) {
//        return bookRepository.findById(id)
//                .map(existing -> {
//                    existing.setTitle(updatedBook.getTitle());
//                    existing.setAuthor(updatedBook.getAuthor());
//                    existing.setYear(updatedBook.getYear());
//                    existing.setPrice(updatedBook.getPrice());
//                    return bookRepository.save(existing);
//                })
//                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
//    }
    public Book updateBook(Long id, Book updatedBookData) throws BookNotFoundException {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        existingBook.setTitle(updatedBookData.getTitle());
        existingBook.setAuthor(updatedBookData.getAuthor());
        existingBook.setYear(updatedBookData.getYear());
        existingBook.setPrice(updatedBookData.getPrice());
        existingBook.setCopies(updatedBookData.getCopies());
        return bookRepository.save(existingBook);
    }
//    public void deleteBook(Long id) {
//        bookRepository.deleteById(id);
//    }
    public void deleteBook(Long id) throws BookNotFoundException {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }
    public List<Book> searchBooks(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
    }
}
