package com.company.etms.controller;

import com.company.etms.entity.User;
import com.company.etms.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AuthController.
 *
 * This class tests:
 * - Login page
 * - Register page
 * - Registration success and failure cases
 *
 * Note:
 * Security is disabled for testing using addFilters = false
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    // MockMvc is used to simulate HTTP requests
    @Autowired
    private MockMvc mockMvc;

    // Mock UserService (no real DB call)
    @MockBean
    private UserService userService;

    // ================== LOGIN TESTS ==================

    /**
     * Test: Login page loads successfully
     */
    @Test
    void testLoginPage() throws Exception {

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())           // HTTP 200
                .andExpect(view().name("login"));     // login.html
    }

    /**
     * Test: Login page with error parameter
     * Should show error message
     */
    @Test
    void testLoginWithError() throws Exception {

        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"));
    }

    // ================== REGISTER PAGE ==================

    /**
     * Test: Register page loads with empty user object
     */
    @Test
    void testRegisterPage() throws Exception {

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    // ================== REGISTER SUCCESS ==================

    /**
     * Test: Successful registration
     * - Email does not exist
     * - Passwords match
     * Expected: redirect to login page
     */
    @Test
    void testRegisterSuccess() throws Exception {

        // Mock: email does not exist
        Mockito.when(userService.emailExists("test@mail.com")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("email", "test@mail.com")
                        .param("password", "1234")
                        .param("confirmPassword", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ================== PASSWORD MISMATCH ==================

    /**
     * Test: Password mismatch during registration
     * Expected: stay on register page with error message
     */
    @Test
    void testRegisterPasswordMismatch() throws Exception {

        mockMvc.perform(post("/register")
                        .param("email", "test@mail.com")
                        .param("password", "1234")
                        .param("confirmPassword", "9999"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("passwordMismatch"));
    }

    // ================== EMAIL EXISTS ==================

    /**
     * Test: Email already exists
     * Expected: show error on register page
     */
    @Test
    void testRegisterEmailExists() throws Exception {

        // Mock: email already exists
        Mockito.when(userService.emailExists("test@mail.com")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("email", "test@mail.com")
                        .param("password", "1234")
                        .param("confirmPassword", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("emailExists"));
    }
}