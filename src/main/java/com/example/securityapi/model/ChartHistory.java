package com.example.securityapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import java.time.LocalDateTime;

@Entity
public class ChartHistory {
    @Id
    @GeneratedValue
    private Long id;

    private String chartType;

    @Lob
    private String chartData; // Store serialized chart input (e.g., JSON)

    private LocalDateTime timestamp;

    // optional: link to user if multi-user
}