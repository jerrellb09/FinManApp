package com.jay.home.finmanapp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a user in the financial management application.
 * 
 * This class stores personal information about users, including their credentials,
 * contact information, income details, and relationships to other entities like
 * accounts, budgets, and bills.
 * 
 * The class uses JPA annotations for database mapping and Jackson annotations
 * for JSON serialization/deserialization control.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User {
    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's email address, used as username for authentication.
     * Must be unique across all users.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * User's password (stored in encrypted form).
     * Excluded from JSON serialization for security.
     */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    /**
     * User's first name.
     */
    @Column(nullable = false)
    private String firstName;

    /**
     * User's last name.
     */
    @Column(nullable = false)
    private String lastName;

    /**
     * User's monthly income amount.
     * Used for budget calculations and financial insights.
     */
    @Column
    private BigDecimal monthlyIncome;
    
    /**
     * Day of the month when the user receives their salary.
     * Used for bill scheduling and cash flow projections.
     */
    @Column
    private Integer paydayDay;

    /**
     * Set of financial accounts associated with this user.
     * Each user can have multiple accounts (checking, savings, etc.).
     * Excluded from JSON serialization to prevent circular references.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Account> accounts = new HashSet<>();

    /**
     * Set of budget allocations created by this user.
     * Each budget is tied to a specific spending category.
     * Excluded from JSON serialization to prevent circular references.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Budget> budgets = new HashSet<>();
    
    /**
     * Set of bills associated with this user.
     * Bills represent recurring payments the user needs to make.
     * Uses JSON managed reference to handle parent-child relationships in serialization.
     */
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Bill> bills = new HashSet<>();
}
