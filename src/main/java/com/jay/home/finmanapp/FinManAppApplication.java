package com.jay.home.finmanapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the FinManApp (Financial Management Application).
 * 
 * This class serves as the entry point for the Spring Boot application, which provides
 * personal finance tracking and budget management capabilities. The application includes
 * features for account integration, transaction tracking, budget management, and bill management.
 * 
 * The @EnableScheduling annotation enables Spring's scheduled task execution capability,
 * which is used for recurring operations like bill payment reminders and transaction syncing.
 */
@SpringBootApplication
@EnableScheduling
public class FinManAppApplication {

    /**
     * Main method that starts the Spring Boot application.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(FinManAppApplication.class, args);
    }
}