package com.jay.home.tradingbotv2.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DecodingException;
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

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Trim any whitespace to prevent base64 decoding errors
            jwt = authorizationHeader.substring(7).trim();
            
            if (jwt.isEmpty()) {
                logger.error("Empty JWT token");
                filterChain.doFilter(request, response);
                return;
            }
            
            try {
                username = jwtUtils.extractUsername(jwt);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token: " + e.getMessage());
            } catch (ExpiredJwtException e) {
                logger.error("JWT Token has expired: " + e.getMessage());
            } catch (MalformedJwtException e) {
                logger.error("Invalid JWT Token: " + e.getMessage());
            } catch (DecodingException e) {
                logger.error("Error decoding JWT Token: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Unexpected error processing JWT Token: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtils.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
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