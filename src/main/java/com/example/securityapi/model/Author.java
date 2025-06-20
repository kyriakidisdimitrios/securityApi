package com.example.securityapi.model;

import jakarta.persistence.*;

import com.example.securityapi.model.Book;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "authors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"first_name", "last_name"})
})
//@Data + @ManyToMany conflict
//Root Cause: @Data generates equals() and hashCode() using all fields
//@ManyToMany causes infinite recursion because Book also has a Set<Author>
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @ManyToMany(mappedBy = "authors")
    private Set<Book> books = new HashSet<>();
    //public Author() {}
//    public Author(String firstName, String lastName) {
//        this.firstName = firstName;
//        this.lastName = lastName;
//    }
//    public Long getId() { return id; }
//    public String getFirstName() { return firstName; }
//    public void setFirstName(String firstName) { this.firstName = firstName; }
//    public String getLastName() { return lastName; }
//    public void setLastName(String lastName) { this.lastName = lastName; }
    public Set<Book> getBooks() { return books; }
    public void setBooks(Set<Book> books) { this.books = books; }
}
