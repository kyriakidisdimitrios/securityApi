package com.example.securityapi.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "authors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"first_name", "last_name"})
})
//@Data + @ManyToMany conflict
//Root Cause: @Data generates equals() and hashCode() using all fields
//@ManyToMany causes infinite recursion because the Book also has a Set<Author>
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
}
