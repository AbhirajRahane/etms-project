package com.company.etms.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles what should happen after a user logs in successfully.
 * It redirects users to different dashboards based on their role.
 */
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    // Logger to track login activity and redirection
    private static final Logger logger = LoggerFactory.getLogger(CustomSuccessHandler.class);

    /**
     * This method is automatically called after successful authentication.
     *
     * It performs the following:
     * 1. Gets the logged-in user's username and role
     * 2. Logs the login success
     * 3. Redirects the user to the correct dashboard based on role
     *
     * @param request        HTTP request object
     * @param response       HTTP response object
     * @param authentication Contains user authentication details
     * @throws IOException      if redirect fails
     * @throws ServletException if servlet error occurs
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Get logged-in username
        String username = authentication.getName();

        // Get user roles (e.g., ROLE_MANAGER, ROLE_EMPLOYEE)
        String role = authentication.getAuthorities().toString();

        // Log successful login
        logger.info("User '{}' logged in successfully with roles: {}", username, role);

        // Check role and redirect accordingly
        if (role.contains("MANAGER")) {
            // Redirect manager to manager dashboard
            logger.info("Redirecting user '{}' to Manager Dashboard", username);
            response.sendRedirect("/manager/dashboard");
        } else {
            // Redirect employee to employee dashboard
            logger.info("Redirecting user '{}' to Employee Dashboard", username);
            response.sendRedirect("/dashboard");
        }
    }
}