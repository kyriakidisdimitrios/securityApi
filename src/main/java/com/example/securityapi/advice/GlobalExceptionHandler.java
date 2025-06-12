package com.example.securityapi.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
//Avoid duplicated error-handling logic
//
//Centralize all controller-level exception handling
//
//Improve maintainability
//@ExceptionHandler(BindException.class) is triggered when Spring fails to bind a request parameter to a method parameter

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class) // No need to trigger
    public String handleValidationError(MethodArgumentNotValidException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Validation error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid input: " + ex.getMessage());
        return "redirect:/admin/books";
    }

    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Binding error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Binding failed: Invalid number format.");
        return "redirect:/admin/books";
    }
}
