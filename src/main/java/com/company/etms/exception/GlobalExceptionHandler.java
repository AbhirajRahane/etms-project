package com.company.etms.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global exception handler for the application.
 *
 * This class handles exceptions thrown from any controller
 * and shows a user-friendly error page instead of crashing.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // Logger to track application errors
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles all unhandled exceptions in the application.
     *
     * Steps:
     * 1. Log the error (for developers)
     * 2. Send error message to UI
     * 3. Show error page
     *
     * @param ex    exception thrown
     * @param model Spring model to pass data to view
     * @return error page
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {

        // ---------------- LOG ERROR ----------------
        // Log full exception details (message + stack trace)
        logger.error("Unhandled exception occurred: {}", ex.getMessage(), ex);

        // ---------------- SEND TO UI ----------------
        // Pass error message to frontend
        model.addAttribute("error", ex.getMessage());

        // Return custom error page
        return "error";
    }
}