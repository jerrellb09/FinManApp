package com.jay.home.finmanapp.config;

import com.jay.home.finmanapp.security.JwtAuthenticationEntryPoint;
import com.jay.home.finmanapp.security.JwtFilter;
import com.jay.home.finmanapp.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * Configuration class for Spring Security settings.
 * 
 * This class configures authentication and authorization for the application,
 * including JWT-based security, password encoding, CORS policies, and endpoint access rules.
 * It establishes a stateless security model using JWT tokens for authentication.
 * 
 * The configuration supports:
 * - JWT-based authentication
 * - CORS configuration for cross-origin requests
 * - Stateless session management
 * - Password encryption
 * - Public/protected endpoint configuration
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Entry point for handling authentication exceptions.
     * This component sends appropriate HTTP responses when authentication fails.
     */
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Creates the JWT authentication filter.
     * This filter intercepts requests and validates JWT tokens.
     * 
     * @param userDetailsService Service to load user details from the database
     * @return Configured JWT filter
     */
    @Bean
    public JwtFilter jwtFilter(UserDetailsService userDetailsService) {
        return new JwtFilter(userDetailsService);
    }

    /**
     * Creates the user details service for authentication.
     * This service loads user-specific data for authentication.
     * 
     * @return Custom implementation of Spring Security's UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }
    
    /**
     * Creates the password encoder for secure password storage.
     * Uses BCrypt hashing algorithm for password encryption.
     * 
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Creates the authentication manager.
     * This manager handles authentication requests and user verification.
     * 
     * @param authConfig Spring's authentication configuration
     * @return Configured authentication manager
     * @throws Exception if there's an issue setting up the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * This allows the frontend application to communicate with the backend API
     * when they are hosted on different domains.
     * 
     * @return Configured CORS source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://localhost:8080",
            "https://fin-man-app.netlify.app" // Replace with your actual Netlify domain
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Configures the main security filter chain.
     * This method sets up the security rules for the application, including:
     * - CSRF protection
     * - CORS configuration
     * - Authentication requirements for different endpoints
     * - Session management
     * - JWT filter integration
     * - Exception handling
     * 
     * @param http HttpSecurity to configure
     * @param jwtFilter The JWT authentication filter to use
     * @return Configured security filter chain
     * @throws Exception if there's an issue configuring security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            // Disable CSRF protection as we're using JWT tokens
            .csrf(csrf -> csrf.disable())
            // Configure CORS using the settings defined in corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Set custom entry point for authentication exceptions
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            // Configure endpoint access rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints that don't require authentication
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // For H2 console access
                .requestMatchers("/error").permitAll() // Error endpoint
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger endpoints
                // NOTE: The following line allows all API endpoints without authentication
                // This is for development convenience and should be removed in production
                .requestMatchers("/api/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated())
            // Use stateless session management (no session cookies)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Add JWT filter before the standard authentication filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            // Disable frame options for H2 console access
            .headers(headers -> headers.frameOptions().disable());
        
        return http.build();
    }
}