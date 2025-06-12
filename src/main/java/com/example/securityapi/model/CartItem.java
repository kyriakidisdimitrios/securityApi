// CartItem.java
package com.example.securityapi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
//@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
//CartItem -> Customer & Book (Many-to-One)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private int quantity;
}