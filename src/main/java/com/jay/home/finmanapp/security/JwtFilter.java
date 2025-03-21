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

public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    private final UserDetailsService userDetailsService;

    public JwtFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

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