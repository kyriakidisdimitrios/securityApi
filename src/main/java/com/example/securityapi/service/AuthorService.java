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
}
