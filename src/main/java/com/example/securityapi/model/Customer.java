package com.example.securityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.securityapi.utilities.CryptoStringConverter;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,32}$",
            message = "Username must be 3–32 characters (letters, digits, dot, underscore, hyphen)")
    private String username;

    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[A-Z][a-z]{2,32}$",
            message = "Name must start with uppercase and be 3–33 letters")
    private String name;

    @NotBlank(message = "Surname is required")
    @Pattern(regexp = "^[A-Z][a-z]{2,32}$",
            message = "Surname must start with uppercase and be 3–33 letters")
    private String surname;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    @Convert(converter = CryptoStringConverter.class)
    private String address;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10 to 15 digits only")
    @Convert(converter = CryptoStringConverter.class)
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    @Convert(converter = CryptoStringConverter.class)
    private String email;

    @NotBlank(message = "Password is required")
    // ✅ FIXED: Removed the @Pattern annotation.
    // The validation is handled by the front-end and the @Valid check in the controller
    // on the initial plain-text password. The database stores the hash, which should not be validated.
    @JsonIgnore
    @ToString.Exclude
    private String password;

    @Column(nullable = false)
    private boolean isAdmin;
}