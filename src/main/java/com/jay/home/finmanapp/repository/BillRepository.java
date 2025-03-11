package com.jay.home.finmanapp.repository;

import com.jay.home.finmanapp.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    List<Bill> findByUserId(Long userId);
    
    List<Bill> findByUserIdAndIsPaid(Long userId, boolean isPaid);
    
    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDay <= :currentDay AND b.isPaid = false")
    List<Bill> findDueBills(@Param("userId") Long userId, @Param("currentDay") int currentDay);
    
    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.user.id = :userId AND b.isPaid = false")
    Double getTotalUnpaidBillsAmount(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.category.id = :categoryId")
    List<Bill> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
    
    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.user.id = :userId AND b.isRecurring = true")
    Double getMonthlyRecurringBillsAmount(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.user.id = :userId AND b.isPaid = true")
    Long countPaidBillsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.user.id = :userId AND b.isPaid = false")
    Long countUnpaidBillsByUserId(@Param("userId") Long userId);
}