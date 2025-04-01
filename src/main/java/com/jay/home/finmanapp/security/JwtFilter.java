package com.jay.home.finmanapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for JWT-based authentication.
 * 
 * This filter intercepts HTTP requests, extracts and validates JWT tokens from
 * Authorization headers, and sets up Spring Security authentication if the token is valid.
 * It ensures that protected endpoints can only be accessed with a valid JWT token.
 * 
 * The filter runs once per request (OncePerRequestFilter) and checks for "Bearer" tokens
 * in the Authorization header. If a valid token is found, it authenticates the user
 * and allows the request to proceed to the protected resources.
 */

public class JwtFilter extends OncePerRequestFilter {

    /**
     * Utility for JWT token operations like validation and extraction.
     */
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Service for loading user details from the database during authentication.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Constructs a JwtFilter with the specified user details service.
     * 
     * @param userDetailsService Service to load user details for token validation
     */
    public JwtFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filters each request to check for and validate JWT tokens.
     * 
     * This method:
     * 1. Extracts the JWT token from the Authorization header
     * 2. Validates the token and extracts the username
     * 3. Loads the user details and validates the token against them
     * 4. Sets up Spring Security authentication if the token is valid
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        
        // Debugging
        boolean hasHeader = authorizationHeader != null;
        if (hasHeader) {
            logger.debug("Authorization header present");
        } else {
            logger.debug("Authorization header not present");
        }

        String username = null;
        String jwt = null;

        if (hasHeader && authorizationHeader.startsWith("Bearer ")) {
            // Trim any whitespace to prevent base64 decoding errors
            jwt = authorizationHeader.substring(7).trim();
            
            if (jwt.isEmpty()) {
                logger.error("Empty JWT token");
                filterChain.doFilter(request, response);
                return;
            }
            
            logger.debug("JWT token found in request");
            
            try {
                username = jwtUtils.extractUsername(jwt);
                logger.debug("Username extracted: " + (username != null ? username : "null"));
            } catch (Exception e) {
                logger.error("Error processing JWT token", e);
            }
        } else {
            logger.debug("No valid JWT token found in request");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtils.validateToken(jwt, userDetails)) {
                    // Store the username (email) as the principal for @AuthenticationPrincipal to work correctly
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                logger.error("Error authenticating with token: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}