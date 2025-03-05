package com.jay.home.tradingbotv2.controller;

import com.jay.home.tradingbotv2.model.User;
import com.jay.home.tradingbotv2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")

public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody Map<String, String> request) {
        User user = userService.createUser(
                request.get("email"),
                request.get("password"),
                request.get("firstName"),
                request.get("lastName")
        );
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(userDetails.getUsername());
        User updatedUser = userService.updateUser(
                user.getId(),
                request.get("firstName"),
                request.get("lastName")
        );
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(userDetails.getUsername());
        userService.changePassword(
                user.getId(),
                request.get("currentPassword"),
                request.get("newPassword")
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(userDetails.getUsername());
        if (userService.verifyPassword(user, request.get("password"))) {
            userService.deleteUser(user.getId());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PatchMapping("/income")
    public ResponseEntity<User> updateIncome(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(userDetails.getUsername());
        
        BigDecimal monthlyIncome = null;
        Integer paydayDay = null;
        
        if (request.containsKey("monthlyIncome")) {
            monthlyIncome = new BigDecimal(request.get("monthlyIncome").toString());
        }
        
        if (request.containsKey("paydayDay")) {
            paydayDay = Integer.parseInt(request.get("paydayDay").toString());
        }
        
        User updatedUser = userService.updateUserIncome(user.getId(), monthlyIncome, paydayDay);
        return ResponseEntity.ok(updatedUser);
    }
    
    @GetMapping("/income")
    public ResponseEntity<Map<String, Object>> getIncome(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(userDetails.getUsername());
        Map<String, Object> response = Map.of(
            "monthlyIncome", user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO,
            "paydayDay", user.getPaydayDay() != null ? user.getPaydayDay() : 0
        );
        return ResponseEntity.ok(response);
    }
}