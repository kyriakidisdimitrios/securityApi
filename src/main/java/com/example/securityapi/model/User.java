package com.example.securityapi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data                   // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor      // Default constructor
@AllArgsConstructor     // All-args constructor
@Builder                // Builder pattern support
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    // ✅ Added for login functionality
    @Column(nullable = false)
    private String password;
}
