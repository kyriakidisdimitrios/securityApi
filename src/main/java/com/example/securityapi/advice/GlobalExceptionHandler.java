package com.example.securityapi.advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import static com.example.securityapi.utilities.LogSanitizer.s;
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationError(MethodArgumentNotValidException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Validation error: {}", s(ex.getMessage()));
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid input: " + ex.getMessage());
        return "redirect:/admin/books";
    }
    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Binding error: {}", s(ex.getMessage()));
        redirectAttributes.addFlashAttribute("errorMessage", "Binding failed: Invalid number format.");
        return "redirect:/admin/books";
    }
    // ✅ General exception handler for unexpected errors (CWE-550 protection)
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, RedirectAttributes redirectAttributes) {
        logger.error("Unexpected error: {}", s(ex.getMessage()), ex);
        redirectAttributes.addFlashAttribute("errorMessage",
                "An unexpected error occurred. Please contact support if the issue persists.");
        return "redirect:/error";
    }
}