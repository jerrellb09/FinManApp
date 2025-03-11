package com.jay.home.finmanapp.repository;

import com.jay.home.finmanapp.model.Budget;
import com.jay.home.finmanapp.model.Category;
import com.jay.home.finmanapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);
    List<Budget> findByUserAndCategory(User user, Category category);
    List<Budget> findByUserAndPeriod(User user, String period);
    List<Budget> findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            User user, LocalDate currentDate, LocalDate currentDate2);
}