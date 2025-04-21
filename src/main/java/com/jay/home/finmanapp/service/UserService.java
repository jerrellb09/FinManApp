package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER") // Basic role for all users
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, String firstName, String lastName) {
        User user = getUserById(id);

        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            user.setLastName(lastName);
        }

        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUserIncome(Long id, java.math.BigDecimal monthlyIncome, Integer paydayDay) {
        User user = getUserById(id);
        
        if (monthlyIncome != null) {
            user.setMonthlyIncome(monthlyIncome);
        }
        
        if (paydayDay != null) {
            user.setPaydayDay(paydayDay);
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = getUserById(id);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Retrieves the demo user account.
     * This account is used for the demo mode functionality.
     * 
     * @return The demo user or null if no demo user exists
     */
    @Transactional(readOnly = true)
    public User getDemoUser() {
        try {
            // First try using the isDemo flag if available (when column exists)
            return userRepository.findByIsDemo(true).orElse(null);
        } catch (Exception e) {
            logger.warn("Error using isDemo flag (column may not exist yet): {}", e.getMessage());
            // Fall back to email lookup if the isDemo column doesn't exist yet
            // This is a temporary fallback until the migration completes
            return userRepository.findByEmail("demo@finmanapp.com").orElse(null);
        }
    }
    
    /**
     * Creates or updates the demo user account with sample data.
     * This method is called during application initialization to ensure
     * a demo account is always available.
     * 
     * @return The created or updated demo user
     */
    @Transactional
    public User setupDemoUser() {
        // Check if demo user already exists
        User demoUser = userRepository.findByIsDemo(true).orElse(null);
        
        if (demoUser == null) {
            // Create new demo user
            demoUser = new User();
            demoUser.setEmail("demo@finmanapp.com");
            demoUser.setPassword(passwordEncoder.encode("demo123")); // This password won't be used directly
            demoUser.setFirstName("Demo");
            demoUser.setLastName("User");
            demoUser.setIsDemo(true);
            demoUser.setMonthlyIncome(new java.math.BigDecimal("5000.00"));
            demoUser.setPaydayDay(15);
        } else {
            // Update existing demo user if needed
            demoUser.setFirstName("Demo");
            demoUser.setLastName("User");
            demoUser.setMonthlyIncome(new java.math.BigDecimal("5000.00"));
            demoUser.setPaydayDay(15);
        }
        
        return userRepository.save(demoUser);
    }
}