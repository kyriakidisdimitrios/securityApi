package com.example.securityapi.service;
import com.example.securityapi.model.Author;
import com.example.securityapi.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AuthorService {
    private final AuthorRepository authorRepository;
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }
    public List<Author> findAll() {
        return authorRepository.findAll();
    }
    // ✅ Check if an author exists by first and last name
    public boolean exists(String firstName, String lastName) {
        return authorRepository.findByFirstNameAndLastName(firstName, lastName).isPresent();
    }
    // ✅ Add a new author
    public void add(Author author) {
        authorRepository.save(author);
    }
    // ✅ Delete author by ID
    public void deleteById(Long id) {
        authorRepository.deleteById(id);
    }
    // ✅ Find author by ID
    public Author findById(Long id) {
        return authorRepository.findById(id).orElse(null);
    }
}