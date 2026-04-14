package com.company.etms.dto;

/**
 * Data Transfer Object (DTO) for User.
 *
 * Used to transfer basic user information (email and role)
 * between layers without exposing the full User entity.
 */
public class UserDto {

    private String email;
    private String role;

    /**
     * Gets the email of the user.
     *
     * @return User email
     */
    public String getEmail() { return email; }

    /**
     * Sets the email of the user.
     *
     * @param email User email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the role of the user.
     *
     * @return User role
     */
    public String getRole() { return role; }

    /**
     * Sets the role of the user.
     *
     * @param role User role
     */
    public void setRole(String role) { this.role = role; }
}