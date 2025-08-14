package com.example.securityapi.service;
import com.example.securityapi.exception.BookNotFoundException;
import com.example.securityapi.model.Author;
import com.example.securityapi.model.Book;
import com.example.securityapi.model.CartItem;
import com.example.securityapi.repository.BookRepository;
import com.example.securityapi.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
@Service
public class BookService {
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;
    public BookService(BookRepository bookRepository, CartItemRepository cartItemRepository) {
        this.bookRepository = bookRepository;
        this.cartItemRepository = cartItemRepository;
    }
    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }
    public Book getBookById(Long id) throws BookNotFoundException {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
    public boolean bookExists(String title, Set<Author> authors, int year) {
        List<Book> books = bookRepository.findByTitleAndYear(title, year);
        for (Book book : books) {
            Set<Author> existingAuthors = book.getAuthors();
            if (existingAuthors != null && existingAuthors.size() == authors.size() && existingAuthors.containsAll(authors)) {
                return true;
            }
        }
        return false;
    }
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }
public Book updateBook(Long id, Book updatedBookData) throws BookNotFoundException {
    Book existingBook = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    existingBook.setTitle(updatedBookData.getTitle());
    existingBook.setAuthors(updatedBookData.getAuthors()); // ✅ Update authors as a Set
    existingBook.setYear(updatedBookData.getYear());
    existingBook.setPrice(updatedBookData.getPrice());
    existingBook.setCopies(updatedBookData.getCopies());
    return bookRepository.save(existingBook);
}
public void deleteBook(Long id) throws BookNotFoundException {
    if (!bookRepository.existsById(id)) {
        throw new BookNotFoundException(id);
    }
    // Remove related cart items first
    List<CartItem> relatedItems = cartItemRepository.findByBookId(id);
    cartItemRepository.deleteAll(relatedItems);
    bookRepository.deleteById(id);
}
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchByTitleOrAuthor(keyword);
    }
}
