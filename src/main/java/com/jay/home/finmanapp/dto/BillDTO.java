package com.jay.home.finmanapp.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for bill information.
 * 
 * This class is used to transfer bill data between the client and server,
 * allowing for a more controlled and efficient API. It includes only the
 * necessary fields for bill operations, avoiding potential circular references
 * and excessive data transfer that might occur with the full entity.
 * 
 * The BillDTO is used in both directions:
 * - From client to server: When creating or updating bills
 * - From server to client: When sending bill information to the client
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {
    /**
     * Unique identifier for the bill.
     * Null when creating a new bill, populated when retrieving or updating.
     */
    private Long id;
    
    /**
     * Name or description of the bill.
     * Examples: "Rent", "Netflix Subscription", "Electric Bill".
     */
    private String name;
    
    /**
     * The amount due for this bill.
     */
    private BigDecimal amount;
    
    /**
     * Day of the month when this bill is due (1-31).
     */
    private int dueDay;
    
    /**
     * Flag indicating whether this bill has been paid for the current cycle.
     */
    private boolean isPaid;
    
    /**
     * Flag indicating whether this bill recurs regularly.
     */
    private boolean isRecurring;
    
    /**
     * ID of the user who owns this bill.
     */
    private Long userId;
    
    /**
     * ID of the category associated with this bill, if any.
     */
    private Long categoryId;
    
    /**
     * Name of the category associated with this bill.
     * Used for display purposes when returning bill information to clients.
     */
    private String categoryName;
}