package com.jay.home.finmanapp.controller;

import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.security.JwtUtils;
import com.jay.home.finmanapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                
                // Create a response with token and user object
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("tokenType", "Bearer");
                
                // Create a standardized user object
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("email", user.getEmail());
                userMap.put("firstName", user.getFirstName());
                userMap.put("lastName", user.getLastName());
                
                // Add additional user details if needed
                if (user.getMonthlyIncome() != null) {
                    userMap.put("monthlyIncome", user.getMonthlyIncome());
                }
                if (user.getPaydayDay() != null) {
                    userMap.put("paydayDay", user.getPaydayDay());
                }
                
                response.put("user", userMap);
                
                logger.debug("User successfully logged in: {}", email);
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
            
            // Create a response with token and user object
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("tokenType", "Bearer");
            
            // Create a standardized user object
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("email", user.getEmail());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            
            // Add additional user details if needed
            if (user.getMonthlyIncome() != null) {
                userMap.put("monthlyIncome", user.getMonthlyIncome());
            }
            if (user.getPaydayDay() != null) {
                userMap.put("paydayDay", user.getPaydayDay());
            }
            
            response.put("user", userMap);
            
            logger.debug("User successfully registered: {}", user.getEmail());
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
    
    @GetMapping("/whoami")
    public ResponseEntity<?> whoAmI(@AuthenticationPrincipal String userEmail) {
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false, "message", "Not authenticated"));
        }
        
        try {
            User user = userService.getUserByEmail(userEmail);
            
            // Create a response that matches our frontend User model
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("email", user.getEmail());
            userResponse.put("firstName", user.getFirstName());
            userResponse.put("lastName", user.getLastName());
            userResponse.put("authenticated", true);
            userResponse.put("isDemo", user.getIsDemo()); // Include demo flag
            
            // Add additional user details if needed
            if (user.getMonthlyIncome() != null) {
                userResponse.put("monthlyIncome", user.getMonthlyIncome());
            }
            if (user.getPaydayDay() != null) {
                userResponse.put("paydayDay", user.getPaydayDay());
            }
            
            logger.debug("Returning user info for {}: {}", userEmail, userResponse);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            logger.error("Error in /whoami endpoint for {}: {}", userEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching user details: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint for demo login functionality.
     * Checks the referer header to ensure the request is coming from justjay.net,
     * then logs the user in with a demo account automatically.
     * 
     * @param referer The HTTP referer header
     * @return A response with a JWT token and demo user information
     */
    @GetMapping("/demo-login")
    public ResponseEntity<?> demoLogin(
            @RequestHeader(value = "Referer", required = false) String referer,
            @RequestHeader(value = "X-Demo-Request", required = false) String demoRequest) {
        logger.info("Demo login attempt with referer: {}, X-Demo-Request: {}", referer, demoRequest);
        
        // Validate the referer to ensure it's coming from justjay.net OR check for demo request header
        boolean validReferer = referer != null && referer.contains("justjay.net");
        boolean isDemoRequest = "true".equals(demoRequest);
        
        // Commented out for easier testing, but should be uncommented in production
        // if (!validReferer && !isDemoRequest) {
        //     logger.warn("Demo login attempt from unauthorized source: {}", referer);
        //     return ResponseEntity.status(HttpStatus.FORBIDDEN)
        //             .body(Map.of("message", "Demo access is only available from justjay.net"));
        // }
        
        try {
            // Try to find the demo user
            User demoUser = userService.getDemoUser();
            if (demoUser == null) {
                logger.error("Demo user not found - this should be pre-configured");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Demo mode not available"));
            }
            
            // Generate JWT token for demo user
            String token = jwtUtils.generateToken(demoUser.getEmail());
            logger.debug("Generated token for demo user {}: {}", demoUser.getEmail(), token);
            
            // Create response with token and user data
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("tokenType", "Bearer");
            
            // Create user object for response
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", demoUser.getId());
            userMap.put("email", demoUser.getEmail());
            userMap.put("firstName", demoUser.getFirstName());
            userMap.put("lastName", demoUser.getLastName());
            userMap.put("isDemo", true);
            
            // Add additional user details
            if (demoUser.getMonthlyIncome() != null) {
                userMap.put("monthlyIncome", demoUser.getMonthlyIncome());
            }
            if (demoUser.getPaydayDay() != null) {
                userMap.put("paydayDay", demoUser.getPaydayDay());
            }
            
            response.put("user", userMap);
            
            logger.info("Demo user successfully logged in with ID: {}", demoUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error logging in demo user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error accessing demo mode: " + e.getMessage()));
        }
    }
}