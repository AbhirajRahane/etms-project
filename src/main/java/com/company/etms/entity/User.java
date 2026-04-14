package com.company.etms.entity;

import jakarta.persistence.*;

/**
 * Entity class representing a User in the system.
 *
 * A user can be:
 * - Manager
 * - Employee
 *
 * This class is mapped to the "user" table in the database.
 */
@Entity
@Table(name = "user")
public class User {

    // Primary key (auto-generated)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User email (used as username for login).
     * Must be unique and cannot be null.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Encrypted password (stored securely in database).
     */
    private String password;

    /**
     * Role of the user.
     * Example values:
     * - ROLE_MANAGER
     * - ROLE_EMPLOYEE
     */
    private String role;

    /**
     * Indicates whether the user account is active.
     * If false, user cannot log in.
     */
    private boolean enabled = true;

    /**
     * Confirm password field.
     * Used only during registration (not stored in DB).
     */
    @Transient
    private String confirmPassword;

    // ---------------- GETTERS & SETTERS ----------------

    /**
     * Get user ID
     * @return user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set user ID
     * @param id user ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get user email
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set user email
     * @param email user email (must be unique)
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get encrypted password
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set user password
     * (Should be encrypted before saving)
     *
     * @param password encrypted password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get user role
     * @return role (ROLE_MANAGER / ROLE_EMPLOYEE)
     */
    public String getRole() {
        return role;
    }

    /**
     * Set user role
     *
     * @param role user role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Check if user account is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable user account
     *
     * @param enabled account status
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get confirm password (used only during registration)
     * @return confirmPassword
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * Set confirm password (used only during registration)
     *
     * @param confirmPassword confirm password
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}