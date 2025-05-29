package com.example.securityapi.service;

import com.example.securityapi.model.User;
import com.example.securityapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
    public boolean authenticateUser(String name, String password) {
        return userRepository.findByNameAndPassword(name, password).isPresent();
    }
}
