package com.company.etms.service;

import com.company.etms.entity.User;
import com.company.etms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for handling user-related operations.
 *
 * This includes:
 * - User registration
 * - Checking existing emails
 * - Fetching user details
 */
@Service
public class UserService {

    // Logger for tracking actions and errors
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // Repository for database operations
    private final UserRepository repo;

    // Password encoder for secure password storage
    private final PasswordEncoder encoder;

    // Constructor injection
    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    /**
     * Register a new user in the system.
     *
     * Steps:
     * 1. Encrypt password
     * 2. Assign default role
     * 3. Save user in database
     *
     * @param user user object to register
     */
    public void registerUser(User user) {
        try {
            logger.info("Registering new user with email: {}", user.getEmail());

            // Encrypt password before saving
            user.setPassword(encoder.encode(user.getPassword()));

            // Set default role (employee)
            user.setRole("ROLE_EMPLOYEE");

            // Save user
            repo.save(user);

            logger.info("User registered successfully with ID: {}", user.getId());

        } catch (Exception e) {
            logger.error("Error while registering user with email: {}", user.getEmail(), e);
            throw e;
        }
    }

    /**
     * Check if an email is already registered.
     *
     * @param email user email
     * @return true if exists, false otherwise
     */
    public boolean emailExists(String email) {

        logger.info("Checking if email exists: {}", email);

        boolean exists = repo.findByEmail(email).isPresent();

        if (exists) {
            logger.warn("Email already exists: {}", email);
        }

        return exists;
    }

    /**
     * Get all users from the system.
     *
     * @return list of users
     */
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return repo.findAll();
    }

    /**
     * Get user by email.
     *
     * @param email user email
     * @return user object or null if not found
     */
    public User getUserByEmail(String email) {

        logger.info("Fetching user by email: {}", email);

        User user = repo.findByEmail(email).orElse(null);

        if (user == null) {
            logger.warn("User not found with email: {}", email);
        }

        return user;
    }

    /**
     * Get user by ID.
     *
     * @param id user ID
     * @return user object or null if not found
     */
    public User getUserById(Long id) {

        logger.info("Fetching user by ID: {}", id);

        User user = repo.findById(id).orElse(null);

        if (user == null) {
            logger.warn("User not found with ID: {}", id);
        }

        return user;
    }
}