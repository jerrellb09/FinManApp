package com.jay.home.tradingbotv2.repository;

import com.jay.home.tradingbotv2.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    List<Bill> findByUserId(Long userId);
    
    List<Bill> findByUserIdAndIsPaid(Long userId, boolean isPaid);
    
    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.dueDay <= :currentDay AND b.isPaid = false")
    List<Bill> findDueBills(@Param("userId") Long userId, @Param("currentDay") int currentDay);
    
    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.user.id = :userId AND b.isPaid = false")
    Double getTotalUnpaidBillsAmount(@Param("userId") Long userId);
}