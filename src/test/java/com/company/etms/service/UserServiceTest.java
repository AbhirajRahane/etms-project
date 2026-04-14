package com.company.etms.service;

import com.company.etms.entity.User;
import com.company.etms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for UserService.
 *
 * This class verifies:
 * - User registration
 * - Email existence check
 * - Fetching users (all / by email / by ID)
 */
class UserServiceTest {

    // Mock repository (no real database)
    @Mock
    private UserRepository repo;

    // Mock password encoder
    @Mock
    private PasswordEncoder encoder;

    // Service with injected mocks
    @InjectMocks
    private UserService userService;

    /**
     * Initialize mocks before each test
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ================== REGISTER USER ==================

    /**
     * Test: Register a new user
     *
     * Expected:
     * - Password is encoded
     * - Default role is set
     * - User is saved to database
     */
    @Test
    void testRegisterUser() {

        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("1234");

        // Mock encoding behavior
        when(encoder.encode("1234")).thenReturn("encoded1234");

        // Call method
        userService.registerUser(user);

        // Verify password is encoded
        assertEquals("encoded1234", user.getPassword());

        // Verify default role assigned
        assertEquals("ROLE_EMPLOYEE", user.getRole());

        // Verify save() is called
        verify(repo, times(1)).save(user);
    }

    // ================== EMAIL EXISTS ==================

    /**
     * Test: Email exists in database
     *
     * Expected:
     * - Method returns true
     */
    @Test
    void testEmailExists_true() {

        when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(new User()));

        boolean result = userService.emailExists("test@mail.com");

        assertTrue(result);
    }

    /**
     * Test: Email does not exist
     *
     * Expected:
     * - Method returns false
     */
    @Test
    void testEmailExists_false() {

        when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        boolean result = userService.emailExists("test@mail.com");

        assertFalse(result);
    }

    // ================== GET ALL USERS ==================

    /**
     * Test: Fetch all users
     *
     * Expected:
     * - Correct number of users returned
     */
    @Test
    void testGetAllUsers() {

        List<User> users = List.of(new User(), new User());

        when(repo.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(repo).findAll();
    }

    // ================== GET USER BY EMAIL ==================

    /**
     * Test: User found by email
     */
    @Test
    void testGetUserByEmail_found() {

        User user = new User();
        user.setEmail("test@mail.com");

        when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("test@mail.com");

        assertNotNull(result);
        assertEquals("test@mail.com", result.getEmail());
    }

    /**
     * Test: User not found by email
     *
     * Expected:
     * - Null is returned
     */
    @Test
    void testGetUserByEmail_notFound() {

        when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        User result = userService.getUserByEmail("test@mail.com");

        assertNull(result);
    }

    // ================== GET USER BY ID ==================

    /**
     * Test: User found by ID
     */
    @Test
    void testGetUserById_found() {

        User user = new User();
        user.setId(1L);

        when(repo.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test: User not found by ID
     *
     * Expected:
     * - Null is returned
     */
    @Test
    void testGetUserById_notFound() {

        when(repo.findById(1L))
                .thenReturn(Optional.empty());

        User result = userService.getUserById(1L);

        assertNull(result);
    }
}