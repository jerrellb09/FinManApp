package com.jay.home.tradingbotv2.controller;

import com.jay.home.tradingbotv2.model.User;
import com.jay.home.tradingbotv2.security.JwtUtils;
import com.jay.home.tradingbotv2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        try {
            User user = userService.getUserByEmail(email);
            
            if (passwordEncoder.matches(password, user.getPassword())) {
                String token = jwtUtils.generateToken(email);
                logger.debug("Generated token for user {}: {}", email, token);
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("tokenType", "Bearer");
                response.put("email", user.getEmail());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Invalid password for user: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid credentials"));
            }
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody Map<String, String> request) {
        try {
            User user = userService.createUser(
                    request.get("email"),
                    request.get("password"),
                    request.get("firstName"),
                    request.get("lastName")
            );
            
            String token = jwtUtils.generateToken(user.getEmail());
            logger.debug("Generated token for new user {}: {}", user.getEmail(), token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        // Simple endpoint to check if authentication is working
        return ResponseEntity.ok(Map.of("message", "Authentication is working"));
    }
}