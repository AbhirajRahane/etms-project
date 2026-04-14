package com.company.etms.config;

import com.company.etms.service.CustomUserDetailsService;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This class configures Spring Security for the application.
 * It defines:
 * - Password encryption
 * - Login and logout behavior
 * - Role-based access control
 */
@Configuration // Marks this class as a Spring configuration class
public class SecurityConfig {

    // Service used to load user details from database
    private final CustomUserDetailsService service;

    /**
     * Constructor-based dependency injection
     *
     * @param service Custom user details service
     */
    public SecurityConfig(CustomUserDetailsService service) {
        this.service = service;
    }

    /**
     * Creates a password encoder bean.
     * This is used to securely hash user passwords before storing in database.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder encoder() {
        // BCrypt is a strong hashing algorithm for passwords
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates a custom success handler bean.
     * This decides where to redirect users after successful login.
     *
     * @return CustomSuccessHandler instance
     */
    @Bean
    public CustomSuccessHandler successHandler() {
        return new CustomSuccessHandler();
    }

    /**
     * Main method to configure security rules.
     *
     * It defines:
     * - Which URLs are public
     * - Which roles can access specific URLs
     * - Login and logout configuration
     *
     * @param http HttpSecurity object
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ---------------- AUTHORIZATION RULES ----------------
            .authorizeHttpRequests(auth -> auth

                // Public URLs (no login required)
                .requestMatchers("/login", "/register", "/css/**").permitAll()

                // Only users with MANAGER role can access these URLs
                .requestMatchers("/manager/**").hasRole("MANAGER")

                // Only users with EMPLOYEE role can access these URLs
                .requestMatchers("/dashboard", "/tasks/**", "/update-status/**")
                .hasRole("EMPLOYEE")

                // Any other request requires authentication (login)
                .anyRequest().authenticated()
            )

            // ---------------- LOGIN CONFIGURATION ----------------
            .formLogin(form -> form

                // Custom login page URL
                .loginPage("/login")

                // Custom logic after successful login (role-based redirect)
                .successHandler(successHandler())

                // Allow all users to access login page
                .permitAll()
            )

            // ---------------- LOGOUT CONFIGURATION ----------------
            .logout(logout -> logout

                // URL to trigger logout
                .logoutUrl("/logout")

                // Redirect to login page after logout
                .logoutSuccessUrl("/login")

                // Allow all users to logout
                .permitAll()
            );

        // Build and return the security configuration
        return http.build();
    }
}