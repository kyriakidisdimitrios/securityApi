package com.example.securityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.securityapi.utilities.CryptoStringConverter; // ✅ PII encryption

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
    // NEW: safe username policy (3–32, letters/digits/._-). Adjust if you allow more chars.
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,32}$",
            message = "Username must be 3–32 characters (letters, digits, dot, underscore, hyphen)")
    private String username;

    @NotBlank(message = "Name is required")
    // NEW: capitalized word 3–33 letters. If you need Greek/Unicode, tell me and I’ll switch regex.
    @Pattern(regexp = "^[A-Z][a-z]{2,32}$",
            message = "Name must start with uppercase and be 3–33 letters")
    private String name;

    @NotBlank(message = "Surname is required")
    // NEW
    @Pattern(regexp = "^[A-Z][a-z]{2,32}$",
            message = "Surname must start with uppercase and be 3–33 letters")
    private String surname;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    // OPTIONAL: uncomment to enforce minimum length
    // @Size(min = 5, max = 255, message = "Address must be 5–255 characters")
    @Convert(converter = CryptoStringConverter.class) // ✅ Encrypt at rest
    private String address;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10 to 15 digits only")
    @Convert(converter = CryptoStringConverter.class) // ✅ Encrypt at rest
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    @Convert(converter = CryptoStringConverter.class) // ✅ Encrypt at rest
    private String email;

    @NotBlank(message = "Password is required")
    // NEW: strong password (8–32, upper/lower/digit/special from @#$%!)
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%!]).{8,32}",
            message = "Password must be 8–32 chars and include upper, lower, number, and special (@#$%!)")
    @JsonIgnore               // Prevent exposure in JSON/API responses
    @ToString.Exclude         // Prevent accidental logging
    private String password;

    @Column(nullable = false)
    private boolean isAdmin;
}
