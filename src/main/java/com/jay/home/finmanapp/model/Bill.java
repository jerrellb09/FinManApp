package com.jay.home.finmanapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Entity class representing a bill in the financial management application.
 * 
 * This class stores information about recurring bills and payments that users need to manage,
 * such as rent, utilities, subscriptions, etc. It tracks details including payment amount,
 * due date, and payment status. The application uses this information to help users plan
 * their finances and receive reminders about upcoming bill payments.
 * 
 * Bills are linked to users and optionally to specific spending categories.
 */
@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Bill {

    /**
     * Unique identifier for the bill in the application's database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name or description of the bill.
     * Examples: "Rent", "Netflix Subscription", "Electric Bill", etc.
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * The amount due for this bill.
     * This represents how much the user needs to pay.
     */
    @Column(nullable = false)
    private BigDecimal amount;
    
    /**
     * Day of the month when this bill is due.
     * Value should be between 1-31. For bills due on the last day of the month,
     * special handling may be applied for months with fewer days.
     */
    @Column(nullable = false)
    private int dueDay;
    
    /**
     * Flag indicating whether this bill has been paid for the current cycle.
     * Reset to false when a new payment cycle begins.
     */
    @Column(nullable = false)
    private boolean isPaid;
    
    /**
     * Flag indicating whether this bill recurs regularly.
     * True for monthly bills like rent, false for one-time payments.
     */
    @Column(nullable = false)
    private boolean isRecurring;

    /**
     * The user who owns this bill.
     * Each bill belongs to a single user.
     * Uses JSON back reference to handle parent-child relationships in serialization.
     */
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The spending category associated with this bill.
     * Optional field to help with categorizing and analyzing bill payments.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}