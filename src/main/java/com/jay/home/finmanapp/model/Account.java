package com.jay.home.finmanapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing a financial account in the application.
 * 
 * This class stores information about user's financial accounts, such as checking accounts,
 * savings accounts, credit cards, etc. It maintains details about the account itself
 * and its connection to external financial institutions through services like Plaid.
 * 
 * Accounts are linked to users and can have multiple transactions associated with them.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    /**
     * Unique identifier for the account in the application's database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this account.
     * Many accounts can belong to a single user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Display name of the account.
     * For example, "Chase Checking" or "Savings Account".
     */
    @Column(nullable = false)
    private String name;

    /**
     * Type of the account.
     * Common values: CHECKING, SAVINGS, CREDIT, INVESTMENT, etc.
     */
    @Column(nullable = false)
    private String type;

    /**
     * Current balance of the account.
     * Updated during synchronization with the financial institution.
     */
    @Column(nullable = false)
    private BigDecimal balance;

    /**
     * External account identifier from the financial data provider (e.g., Plaid).
     * This is the ID used to reference this account in the external system.
     */
    @Column(nullable = false)
    private String accountId;

    /**
     * Access token for the financial data integration.
     * Used for authenticating API requests to the external service.
     */
    @Column(nullable = false)
    private String accessToken;

    /**
     * Identifier for the financial institution where this account is held.
     * Used for categorizing accounts by institution.
     */
    @Column(nullable = false)
    private String institutionId;

    /**
     * Name of the financial institution where this account is held.
     * For example, "Chase", "Bank of America", etc.
     */
    @Column(nullable = false)
    private String institutionName;

    /**
     * Timestamp of the last successful synchronization with the financial institution.
     * Used to track when account data was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime lastSynced;
}