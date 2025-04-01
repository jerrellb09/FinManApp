package com.jay.home.finmanapp.repository;

import com.jay.home.finmanapp.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Bill entity operations.
 * 
 * This interface provides methods for accessing and modifying bill data in the database.
 * It extends JpaRepository to inherit standard CRUD operations and adds custom query methods
 * for bill-specific operations such as finding bills by user, finding due bills, and 
 * calculating bill statistics.
 * 
 * Spring Data JPA automatically implements this interface at runtime, generating the
 * necessary SQL queries based on method names and annotations.
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    /**
     * Finds all bills associated with a specific user.
     *
     * @param userId The ID of the user whose bills to retrieve
     * @return A list of all bills owned by the specified user
     */
    List<Bill> findByUserId(Long userId);
    
    /**
     * Finds bills for a user filtered by payment status.
     *
     * @param userId The ID of the user whose bills to retrieve
     * @param isPaid The payment status to filter by (true for paid bills, false for unpaid)
     * @return A list of bills matching both the user ID and payment status
     */
    List<Bill> findByUserIdAndIsPaid(Long userId, boolean isPaid);
    
    /**
     * Finds unpaid bills that are due on or before the current day for a specific user.
     * Uses a JPQL query to filter bills based on due day, payment status, and user.
     *
     * @param userId The ID of the user whose bills to check
     * @param currentDay The current day of the month
     * @return A list of unpaid bills that are due today or overdue
     */
    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDay <= :currentDay AND b.isPaid = false")
    List<Bill> findDueBills(@Param("userId") Long userId, @Param("currentDay") int currentDay);
    
    /**
     * Calculates the total amount of all unpaid bills for a specific user.
     * Uses a JPQL query with SUM aggregate function.
     *
     * @param userId The ID of the user whose unpaid bills to sum
     * @return The total amount of unpaid bills, or null if there are no unpaid bills
     */
    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.user.id = :userId AND b.isPaid = false")
    Double getTotalUnpaidBillsAmount(@Param("userId") Long userId);
    
    /**
     * Finds bills for a user that belong to a specific category.
     * Uses a JPQL query to filter by both user ID and category ID.
     *
     * @param userId The ID of the user whose bills to retrieve
     * @param categoryId The ID of the category to filter by
     * @return A list of bills matching both the user and category criteria
     */
    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.category.id = :categoryId")
    List<Bill> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
    
    /**
     * Calculates the total amount of all recurring bills for a specific user.
     * This represents the user's fixed monthly expenses.
     *
     * @param userId The ID of the user whose recurring bills to sum
     * @return The total amount of recurring bills, or null if there are no recurring bills
     */
    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.user.id = :userId AND b.isRecurring = true")
    Double getMonthlyRecurringBillsAmount(@Param("userId") Long userId);
    
    /**
     * Counts the number of paid bills for a specific user.
     * Useful for dashboard statistics and payment tracking.
     *
     * @param userId The ID of the user whose paid bills to count
     * @return The count of paid bills
     */
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.user.id = :userId AND b.isPaid = true")
    Long countPaidBillsByUserId(@Param("userId") Long userId);
    
    /**
     * Counts the number of unpaid bills for a specific user.
     * Useful for alerts and payment tracking.
     *
     * @param userId The ID of the user whose unpaid bills to count
     * @return The count of unpaid bills
     */
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.user.id = :userId AND b.isPaid = false")
    Long countUnpaidBillsByUserId(@Param("userId") Long userId);
}