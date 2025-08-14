package com.example.securityapi.exception; // Or your preferred exception package
public class CartItemException extends RuntimeException {
    public CartItemException(String message) {
        super(message);
    }
}