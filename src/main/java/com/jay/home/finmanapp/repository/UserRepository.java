package com.jay.home.finmanapp.repository;

import com.jay.home.finmanapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * 
 * This interface provides methods for accessing and modifying user data in the database.
 * It extends JpaRepository to inherit standard CRUD operations and adds custom query methods
 * for user-specific operations such as finding users by email and checking email existence.
 * 
 * Spring Data JPA automatically implements this interface at runtime, generating the
 * necessary SQL queries based on method names.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by their email address.
     * 
     * This method is used primarily for authentication and user lookup.
     * Email addresses are unique in the system, so this will return at most one user.
     *
     * @param email The email address to search for
     * @return An Optional containing the user if found, or empty if no user exists with that email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if a user exists with the given email address.
     * 
     * This is used during registration to prevent duplicate email addresses.
     *
     * @param email The email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Finds a user marked as demo user.
     * 
     * Used for demo mode functionality to retrieve the dedicated demo account.
     * 
     * @param isDemo Boolean flag indicating if the user is a demo user
     * @return An Optional containing the demo user if found, or empty if no demo user exists
     */
    Optional<User> findByIsDemo(Boolean isDemo);
}