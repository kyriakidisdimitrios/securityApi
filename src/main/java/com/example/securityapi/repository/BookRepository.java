package com.example.securityapi.repository;

import com.example.securityapi.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface BookRepository extends JpaRepository<Book, Long> {
    // Find by exact title
    Optional<Book> findByTitle(String title);
    // Find all books by a given author
    @Query("SELECT b FROM Book b JOIN b.authors a " +
            "WHERE a.firstName = :firstName AND a.lastName = :lastName")
    List<Book> findByAuthorName(@Param("firstName") String firstName, @Param("lastName") String lastName); //“Bind the value of this method argument firstName to the query parameter: firstName.”
    //boolean existsByTitleAndAuthorAndYear(String title, String author, int year);
    List<Book> findByTitleAndYear(String title, int year);
    @Query("""
    SELECT DISTINCT b FROM Book b
    JOIN b.authors a
    WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Book> searchByTitleOrAuthor(@Param("keyword") String keyword);
    //E.g., searchByTitleOrAuthor("row"); Books with title like "The Growth of Data". Books by authors like "Rowling", "Rowe", etc.
}
