package com.jay.home.finmanapp.config;

import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.service.UserService;
import com.jay.home.finmanapp.service.DemoDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

@Configuration
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private Environment env;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DemoDataService demoDataService;
    
    @EventListener(ApplicationStartedEvent.class)
    public void checkDatabaseBeforeStartup() {
        // Check active profiles to determine database type
        String[] activeProfiles = env.getActiveProfiles();
        boolean isH2 = activeProfiles.length > 0 && Arrays.asList(activeProfiles).contains("h2");
        
        if (isH2) {
            System.out.println("Using H2 in-memory database. No database verification needed.");
            return;
        }
        
        System.out.println("Verifying PostgreSQL connection before starting application...");
        
        // Try to create database if it doesn't exist (requires PostgreSQL superuser)
        try {
            String url = env.getProperty("spring.datasource.url");
            String username = env.getProperty("spring.datasource.username");
            String password = env.getProperty("spring.datasource.password");
            
            if (url == null || username == null || password == null) {
                throw new RuntimeException("Database connection properties not found in environment");
            }
            
            // Extract database name from JDBC URL
            String dbName = url.substring(url.lastIndexOf("/") + 1);
            
            // Connect to PostgreSQL server (not specific database)
            String baseUrl = url.substring(0, url.lastIndexOf("/") + 1) + "postgres";
            
            try {
                // Use reflection to avoid direct dependency on PostgreSQL driver class
                Class<?> driverClass = Class.forName("org.postgresql.Driver");
                Object driver = driverClass.getDeclaredConstructor().newInstance();
                SimpleDriverDataSource dataSource = new SimpleDriverDataSource((java.sql.Driver)driver, baseUrl, username, password);
                
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                     
                    // Check if database exists
                    System.out.println("Checking if database '" + dbName + "' exists...");
                    stmt.execute("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
                    
                    if (!stmt.getResultSet().next()) {
                        System.out.println("Database '" + dbName + "' does not exist. Attempting to create it...");
                        stmt.execute("CREATE DATABASE " + dbName);
                        System.out.println("Database '" + dbName + "' created successfully.");
                    } else {
                        System.out.println("Database '" + dbName + "' already exists.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Failed to create/verify database: " + e.getMessage());
                System.err.println("Please run ./fix-postgres.sh to fix this issue.");
                // We don't throw an exception here - let the application try to connect anyway
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                System.err.println("Failed to load PostgreSQL driver: " + e.getMessage());
                System.err.println("Make sure PostgreSQL driver is in the classpath");
            }
        } catch (Exception e) {
            System.err.println("Error verifying database connection: " + e.getMessage());
        }
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDatabase() {
        String[] activeProfiles = env.getActiveProfiles();
        boolean isH2 = activeProfiles.length > 0 && Arrays.asList(activeProfiles).contains("h2");
        
        try {
            // Basic validation query to check database connection
            String dbType = isH2 ? "H2" : "PostgreSQL";
            logger.info("Connected to {} database", dbType);
            
            // For PostgreSQL, we need to handle schemas differently
            if (!isH2) {
                // Check if we need to set up the PostgreSQL database
                try {
                    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pg_catalog.pg_tables WHERE schemaname = 'public'", Integer.class);
                    logger.info("PostgreSQL schema exists and is accessible.");
                } catch (Exception e) {
                    logger.info("Setting up PostgreSQL schema...");
                    // This is just a placeholder, JPA will handle schema creation
                }
            }
            
            // Ensure the demo user exists for demo mode functionality
            ensureDemoUser();
        } catch (Exception e) {
            logger.error("Error initializing database: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Ensures that a demo user exists in the database.
     * This is crucial for the demo mode functionality.
     */
    private void ensureDemoUser() {
        try {
            logger.info("Ensuring demo user exists...");
            
            // First try the JDBC direct approach
            boolean demoUserCreated = userService.ensureDemoUserExists();
            
            if (demoUserCreated) {
                logger.info("Demo user successfully created or verified via JDBC");
            } else {
                // If JDBC approach failed, try JPA
                logger.info("Attempting to create demo user via JPA...");
                User demoUser = userService.setupDemoUser();
                
                if (demoUser == null) {
                    logger.warn("Failed to create or verify demo user");
                    return;
                }
                
                logger.info("Demo user successfully created or verified via JPA with ID: {}", demoUser.getId());
            }
            
            // Once demo user exists, initialize demo data for this user
            initializeDemoData();
        } catch (Exception e) {
            logger.error("Error ensuring demo user exists: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Initializes demo data for the demo user account.
     */
    private void initializeDemoData() {
        try {
            logger.info("Initializing demo data...");
            boolean initialized = demoDataService.initializeDemoUserData();
            
            if (initialized) {
                logger.info("Demo data successfully initialized");
            } else {
                logger.warn("Failed to initialize demo data");
            }
        } catch (Exception e) {
            logger.error("Error initializing demo data: {}", e.getMessage(), e);
        }
    }
}