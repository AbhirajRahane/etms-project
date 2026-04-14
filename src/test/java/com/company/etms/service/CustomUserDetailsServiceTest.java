package com.company.etms.service;

import com.company.etms.entity.User;
import com.company.etms.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for CustomUserDetailsService.
 *
 * This class verifies the login logic used by Spring Security:
 * - When user exists → return UserDetails
 * - When user does not exist → throw exception
 */
class CustomUserDetailsServiceTest {

    // Fake repository (simulates database)
    @Mock
    private UserRepository repo;

    // Service class with injected mock repository
    @InjectMocks
    private CustomUserDetailsService service;

    /**
     * Setup method to initialize mocks before each test.
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test: Load user successfully from database
     *
     * Flow:
     * - Mock a user in DB
     * - Call service method
     * - Verify returned UserDetails
     *
     * Expected:
     * - UserDetails is not null
     * - Username, password, and role are correct
     */
    @Test
    void testLoadUserByUsername_success() {

        // Create sample user
        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("encoded123");
        user.setRole("ROLE_EMPLOYEE");

        // Mock repository response
        when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        // Call service method
        UserDetails result = service.loadUserByUsername("test@mail.com");

        // Validate result
        assertNotNull(result);
        assertEquals("test@mail.com", result.getUsername());
        assertEquals("encoded123", result.getPassword());

        // Check if role is correctly assigned
        assertTrue(result.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    /**
     * Test: User not found in database
     *
     * Flow:
     * - Mock empty DB response
     * - Call service method
     *
     * Expected:
     * - UsernameNotFoundException should be thrown
     */
    @Test
    void testLoadUserByUsername_userNotFound() {

        // Mock repository returns no user
        when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        // Verify exception is thrown
        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("test@mail.com");
        });
    }
}