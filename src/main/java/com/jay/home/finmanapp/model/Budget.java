package com.jay.home.finmanapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity class representing a budget allocation in the application.
 * 
 * This class stores information about budgets set by users for different categories
 * of spending. Budgets define spending limits for specific time periods and can be
 * used to track progress against financial goals. The application uses budgets to
 * generate alerts when spending approaches or exceeds the defined limits.
 * 
 * Budgets are linked to users and optionally to specific spending categories.
 */
@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    /**
     * Unique identifier for the budget in the application's database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who created and owns this budget.
     * Many budgets can belong to a single user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Display name for the budget.
     * For example, "Grocery Budget" or "Entertainment Spending".
     */
    @Column(nullable = false)
    private String name;

    /**
     * The monetary amount allocated for this budget.
     * This represents the spending limit for the specified period.
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * The spending category associated with this budget.
     * Optional because a budget might cover multiple categories or a custom grouping.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    /**
     * Time period for which the budget applies.
     * Common values: DAILY, WEEKLY, MONTHLY, ANNUALLY.
     */
    @Column(nullable = false)
    private String period;

    /**
     * Date when this budget becomes active.
     * Used for time-based filtering and analysis.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Optional end date for the budget.
     * If not specified, the budget is considered ongoing/recurring.
     */
    @Column
    private LocalDate endDate;

    /**
     * Threshold at which to warn the user about approaching their budget limit.
     * Expressed as a percentage of the total budget amount (e.g., 80 for 80%).
     */
    @Column(nullable = false)
    private BigDecimal warningThreshold;
}