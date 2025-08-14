package com.example.securityapi.model;
import jakarta.persistence.*;
import lombok.*; // Import Lombok if you want to use its annotations
import java.time.LocalDateTime;
@Entity
@Table(name = "chart_history") // Good practice to name the table explicitly
@Getter
@Setter
@NoArgsConstructor // Recommended Lombok annotations
//@Data
@AllArgsConstructor
public class CartHistory {
    @Id
    @GeneratedValue
    private Long id;
    // --- THIS IS THE NEW CONNECTION ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    private String chartType;
    @Lob
    private String chartData;
    private LocalDateTime timestamp;
}