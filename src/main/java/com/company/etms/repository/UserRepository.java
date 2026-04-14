package com.company.etms.repository;

import com.company.etms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for User entities.
 * Provides CRUD operations and custom queries for users.
 * 
 * Extends JpaRepository<User, Long>:
 * - User: the entity type
 * - Long: the type of the primary key (id)
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Returns an Optional because the user may or may not exist.
     *
     * @param email The email to search for
     * @return Optional<User> containing the user if found
     */
    Optional<User> findByEmail(String email);
}