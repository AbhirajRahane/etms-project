package com.company.etms.service;

import com.company.etms.entity.User;
import com.company.etms.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

// Logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class used by Spring Security for authentication.
 *
 * This class loads user details (email, password, role)
 * from the database during login.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Logger to track login activity
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    // Repository to access user data
    private final UserRepository repo;

    // Constructor injection
    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    /**
     * Loads user details from database using email.
     *
     * This method is automatically called by Spring Security
     * during login authentication.
     *
     * Steps:
     * 1. Check if user exists in database
     * 2. If not, throw exception
     * 3. If yes, convert user into Spring Security format
     *
     * @param email user's email (used as username)
     * @return UserDetails object for authentication
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // ---------------- LOGIN ATTEMPT ----------------
        logger.info("Authentication attempt for email: {}", email);

        // ---------------- FETCH USER ----------------
        User user = repo.findByEmail(email)
                .orElseThrow(() -> {

                    // If user not found
                    logger.warn("User not found with email: {}", email);

                    // Throw exception (Spring Security handles it)
                    return new UsernameNotFoundException("User not found");
                });

        // ---------------- USER FOUND ----------------
        logger.info("User found. Proceeding with authentication for email: {}", email);

        // ---------------- CONVERT TO SPRING SECURITY USER ----------------
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())   // email as username
                .password(user.getPassword())    // encrypted password
                .roles(user.getRole().replace("ROLE_", "")) // remove ROLE_ prefix
                .build();
    }
}