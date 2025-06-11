package com.example.securityapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // <-- IMPORT THESE ANNOTATIONS
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty(message = "Title cannot be empty.")
    private String title;
    @NotNull(message = "Year is required.")
    private int year;

    @NotNull(message = "Price is required.")
    @Positive(message = "Price must be a positive number.")
    private double price;

    @NotNull(message = "Number of copies is required.")
    @Min(value = 0, message = "There must be at least 0 copies.")
    private int copies;

    @NotEmpty(message = "At least one author must be selected.")
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();
    public Book() {}
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }
    public Set<Author> getAuthors() { return authors; }
    public void setAuthors(Set<Author> authors) { this.authors = authors; }
}