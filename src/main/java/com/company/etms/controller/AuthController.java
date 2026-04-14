package com.company.etms.controller;

import com.company.etms.entity.User;
import com.company.etms.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling user authentication.
 * It manages:
 * - Login page display
 * - Registration page display
 * - New user registration process
 */
@Controller
public class AuthController {

    // Logger to track user actions like login and registration
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Service layer to handle user-related operations
    private final UserService service;

    /**
     * Constructor-based dependency injection
     *
     * @param service UserService instance
     */
    public AuthController(UserService service) {
        this.service = service;
    }

    /**
     * Displays the login page.
     * If login fails, shows an error message.
     *
     * @param error  indicates login failure (if any)
     * @param model  Spring model to pass data to view
     * @return login page
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {

        // Log access to login page
        logger.info("Login page accessed");

        // If there is an error, show message
        if (error != null) {
            logger.warn("Login failed attempt detected");
            model.addAttribute("errorMessage", "Incorrect email or password");
        }

        return "login";
    }

    /**
     * Displays the registration page.
     *
     * @param model Spring model to bind form data
     * @return registration page
     */
    @GetMapping("/register")
    public String register(Model model) {

        // Log access to registration page
        logger.info("Registration page accessed");

        // Add empty user object for form binding
        model.addAttribute("user", new User());

        return "register";
    }

    /**
     * Handles user registration form submission.
     *
     * Steps:
     * 1. Check if passwords match
     * 2. Check if email already exists
     * 3. Save user if valid
     * 4. Redirect to login page
     *
     * @param user             user data from form
     * @param confirmPassword  confirm password field
     * @param model            Spring model for error messages
     * @return redirect or registration page based on validation
     */
    @PostMapping("/register")
    public String saveUser(@ModelAttribute("user") User user,
                           @RequestParam("confirmPassword") String confirmPassword,
                           Model model) {

        // Log registration attempt
        logger.info("Registration attempt for email: {}", user.getEmail());

        // ---------------- PASSWORD VALIDATION ----------------
        // Check if password and confirm password match
        if (!user.getPassword().equals(confirmPassword)) {
            logger.warn("Password mismatch for email: {}", user.getEmail());

            model.addAttribute("passwordMismatch", "Passwords do not match!");
            return "register";
        }

        // ---------------- EMAIL VALIDATION ----------------
        // Check if email already exists in database
        if (service.emailExists(user.getEmail())) {
            logger.warn("Registration failed - email already exists: {}", user.getEmail());

            model.addAttribute("emailExists", "Email is already registered!");
            return "register";
        }

        // ---------------- SAVE USER ----------------
        // Save the new user to database
        service.registerUser(user);

        logger.info("User registered successfully: {}", user.getEmail());

        // Redirect to login page after successful registration
        return "redirect:/login";
    }
}