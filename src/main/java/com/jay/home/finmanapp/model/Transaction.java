package com.jay.home.finmanapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing a financial transaction in the application.
 * 
 * This class stores information about individual financial transactions, such as
 * purchases, deposits, transfers, etc. It captures details about the transaction
 * including amount, date, category, and source account. Transactions can be imported
 * from financial institutions or manually entered by users.
 * 
 * Transactions are linked to accounts and categories for organization and analysis.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    /**
     * Unique identifier for the transaction in the application's database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The financial account associated with this transaction.
     * Each transaction belongs to a single account.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * External transaction identifier from the financial data provider.
     * Used to prevent duplicate imports when syncing with external services.
     */
    @Column(nullable = false)
    private String transactionId;

    /**
     * Description of the transaction.
     * This typically includes the merchant name or purpose of the transaction.
     */
    @Column(nullable = false)
    private String description;

    /**
     * Monetary amount of the transaction.
     * Positive values represent income/deposits, negative values represent expenses/withdrawals.
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * Date and time when the transaction occurred.
     * Used for chronological sorting and time-based analysis.
     */
    @Column(nullable = false)
    private LocalDateTime date;

    /**
     * Category assigned to this transaction.
     * Used for organizing transactions by type (e.g., Food, Transportation, Housing).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Flag indicating whether this transaction was manually entered by the user.
     * False means the transaction was imported from a financial institution.
     */
    @Column
    private boolean isManualEntry;
}