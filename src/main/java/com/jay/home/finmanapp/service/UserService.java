package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
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
     * User row mapper for mapping database rows to User objects directly.
     * This is used when we need to bypass JPA for schema compatibility issues.
     */
    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            
            // Handle nullable columns
            try {
                user.setMonthlyIncome(rs.getBigDecimal("monthly_income"));
            } catch (SQLException e) {
                // Column might be null, which is acceptable
                user.setMonthlyIncome(null);
            }
            
            try {
                user.setPaydayDay(rs.getInt("payday_day"));
                if (rs.wasNull()) {
                    user.setPaydayDay(null);
                }
            } catch (SQLException e) {
                // Column might be null, which is acceptable
                user.setPaydayDay(null);
            }
            
            // Handle the isDemo column which might not exist yet
            try {
                user.setIsDemo(rs.getBoolean("is_demo"));
            } catch (SQLException e) {
                // Column doesn't exist yet, set default
                user.setIsDemo(false);
            }
            
            return user;
        }
    }
    
    /**
     * Retrieves the demo user account using direct JDBC.
     * This method bypasses JPA to avoid schema compatibility issues.
     * 
     * @return The demo user or null if no demo user exists
     */
    public User getDemoUserJdbc() {
        try {
            // First try with the is_demo column
            String sql = "SELECT * FROM users WHERE is_demo = true LIMIT 1";
            try {
                return jdbcTemplate.queryForObject(sql, new UserRowMapper());
            } catch (DataAccessException e) {
                // The query might fail if is_demo column doesn't exist
                logger.warn("Error querying with is_demo (column may not exist): {}", e.getMessage());
                
                // Try with email directly
                sql = "SELECT * FROM users WHERE email = 'demo@finmanapp.com' LIMIT 1";
                try {
                    return jdbcTemplate.queryForObject(sql, new UserRowMapper());
                } catch (DataAccessException e2) {
                    logger.warn("Demo user not found by email: {}", e2.getMessage());
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error retrieving demo user: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates a demo user with raw SQL if it doesn't exist.
     * This method bypasses JPA to avoid schema compatibility issues.
     * 
     * @return True if the demo user was created or already exists, false otherwise
     */
    public boolean ensureDemoUserExists() {
        try {
            // First check if demo user exists
            User existingUser = getDemoUserJdbc();
            if (existingUser != null) {
                logger.info("Demo user already exists with ID: {}", existingUser.getId());
                return true;
            }
            
            // Check if is_demo column exists
            boolean isDemoColumnExists = doesColumnExist("users", "is_demo");
            logger.info("is_demo column exists: {}", isDemoColumnExists);
            
            // Add the is_demo column if it doesn't exist
            if (!isDemoColumnExists) {
                try {
                    String alterTableSql = "ALTER TABLE users ADD COLUMN is_demo BOOLEAN NOT NULL DEFAULT FALSE";
                    jdbcTemplate.execute(alterTableSql);
                    logger.info("Added is_demo column to users table");
                } catch (Exception e) {
                    logger.warn("Failed to add is_demo column: {}", e.getMessage());
                    // Continue anyway - we'll create the user without relying on this column
                }
            }
            
            // Check other required columns
            boolean monthlyIncomeExists = doesColumnExist("users", "monthly_income");
            boolean paydayDayExists = doesColumnExist("users", "payday_day");
            
            // Construct the INSERT SQL based on available columns
            StringBuilder sqlBuilder = new StringBuilder("INSERT INTO users (email, password, first_name, last_name");
            if (isDemoColumnExists) sqlBuilder.append(", is_demo");
            if (monthlyIncomeExists) sqlBuilder.append(", monthly_income");
            if (paydayDayExists) sqlBuilder.append(", payday_day");
            sqlBuilder.append(") VALUES ('demo@finmanapp.com', ?, 'Demo', 'User'");
            if (isDemoColumnExists) sqlBuilder.append(", true");
            if (monthlyIncomeExists) sqlBuilder.append(", 5000.00");
            if (paydayDayExists) sqlBuilder.append(", 15");
            sqlBuilder.append(")");
            
            String sql = sqlBuilder.toString();
            logger.info("Creating demo user with SQL: {}", sql);
            
            // Execute the insert
            int rowsAffected = jdbcTemplate.update(
                sql,
                passwordEncoder.encode("demo123")
            );
            
            logger.info("Demo user created successfully: {} rows affected", rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.error("Failed to ensure demo user exists: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Utility method to check if a column exists in a table.
     * 
     * @param tableName The name of the table
     * @param columnName The name of the column to check
     * @return True if the column exists, false otherwise
     */
    private boolean doesColumnExist(String tableName, String columnName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.columns " +
                         "WHERE table_name = ? AND column_name = ?";
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.warn("Error checking if column {} exists in table {}: {}", 
                      columnName, tableName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves the demo user account by email (fallback method).
     * This is used when the isDemo column doesn't exist yet.
     * 
     * @return The demo user or null if no demo user exists
     */
    @Transactional(readOnly = true, propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public User getDemoUserByEmail() {
        return userRepository.findByEmail("demo@finmanapp.com").orElse(null);
    }
    
    /**
     * Retrieves the demo user account.
     * This account is used for the demo mode functionality.
     * 
     * @return The demo user or null if no demo user exists
     */
    @Transactional(readOnly = true)
    public User getDemoUser() {
        // First try the direct JDBC approach which is most resilient
        User demoUser = getDemoUserJdbc();
        if (demoUser != null) {
            return demoUser;
        }
        
        // Fall back to JPA methods if JDBC failed
        try {
            // Try using the isDemo flag if available
            return userRepository.findByIsDemo(true).orElse(null);
        } catch (Exception e) {
            logger.warn("Error using isDemo flag (column may not exist yet): {}", e.getMessage());
            // Fall back to email lookup in a new transaction
            return getDemoUserByEmail();
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
        // First try using raw JDBC for maximum compatibility
        if (ensureDemoUserExists()) {
            return getDemoUserJdbc();
        }
        
        // If direct JDBC approach fails, fall back to JPA
        try {
            // Check if demo user already exists
            User demoUser = null;
            try {
                demoUser = userRepository.findByIsDemo(true).orElse(null);
            } catch (Exception e) {
                logger.warn("Error finding demo user by isDemo flag: {}", e.getMessage());
                demoUser = userRepository.findByEmail("demo@finmanapp.com").orElse(null);
            }
            
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
                demoUser.setIsDemo(true); // Ensure it's marked as demo
                demoUser.setMonthlyIncome(new java.math.BigDecimal("5000.00"));
                demoUser.setPaydayDay(15);
            }
            
            return userRepository.save(demoUser);
        } catch (Exception e) {
            logger.error("Failed to set up demo user via JPA: {}", e.getMessage());
            return null;
        }
    }
}