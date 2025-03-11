package com.jay.home.finmanapp.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;

/**
 * A simpler database initializer that doesn't depend directly on PostgreSQL driver.
 * This is an alternative to the more complex DatabaseConfig class.
 */
@Configuration
@ConditionalOnProperty(name = "spring.jpa.database-platform", havingValue = "org.hibernate.dialect.PostgreSQLDialect")
public class SimpleDatabaseInitializer {

    @Autowired
    private Environment env;
    
    @PostConstruct
    public void initializeDatabase() {
        // Only run for PostgreSQL, not for H2
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles.length > 0 && Arrays.asList(activeProfiles).contains("h2")) {
            System.out.println("Using H2 database, skipping PostgreSQL initialization");
            return;
        }
        
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        
        if (url == null || username == null || password == null) {
            System.err.println("Database properties not found in configuration");
            return;
        }
        
        // Try to extract database name from URL
        String dbName = null;
        try {
            dbName = url.substring(url.lastIndexOf("/") + 1);
            System.out.println("Database name extracted from URL: " + dbName);
        } catch (Exception e) {
            System.err.println("Could not extract database name from URL: " + url);
            return;
        }
        
        // Try to connect to the 'postgres' database to create our database
        String postgresUrl = url.substring(0, url.lastIndexOf("/") + 1) + "postgres";
        
        try {
            System.out.println("Checking if PostgreSQL database exists: " + dbName);
            
            // First try to connect to the actual database to see if it exists
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                System.out.println("Connected to database: " + dbName);
                System.out.println("Database already exists, no need to create it");
                return;
            } catch (Exception e) {
                System.out.println("Could not connect to database: " + e.getMessage());
                System.out.println("Attempting to create database...");
            }
            
            // Connect to postgres database to create our database
            try (Connection conn = DriverManager.getConnection(postgresUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("Connected to postgres database");
                System.out.println("Creating database: " + dbName);
                
                // Create the database if it doesn't exist
                stmt.execute("CREATE DATABASE " + dbName);
                System.out.println("Database created successfully: " + dbName);
            }
            
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            System.err.println("Please make sure PostgreSQL is running and accessible");
            System.err.println("Or run ./fix-postgres.sh to initialize the database");
        }
    }
}