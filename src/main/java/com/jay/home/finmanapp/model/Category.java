package com.jay.home.finmanapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entity class representing a transaction category in the application.
 * 
 * This class defines categories for classifying financial transactions, such as 
 * "Food", "Transportation", "Housing", etc. Categories help organize transactions
 * and enable budget creation and spending analysis by type of expense.
 * 
 * Categories are global and shared across all users, though users can create
 * their own custom categories as well.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    /**
     * Unique identifier for the category in the application's database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the category.
     * This is unique across the application to prevent duplicate categories.
     * Examples include "Housing", "Food", "Transportation", etc.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Optional description of the category.
     * Provides additional context about what types of transactions belong in this category.
     */
    @Column
    private String description;

    /**
     * URL or reference to an icon representing this category.
     * Used in the UI to visually identify categories.
     */
    @Column
    private String iconUrl;
}