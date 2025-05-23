package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.Account;
import com.jay.home.finmanapp.model.Budget;
import com.jay.home.finmanapp.model.Category;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.repository.BudgetRepository;
import com.jay.home.finmanapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Autowired
    public BudgetService(
            BudgetRepository budgetRepository,
            TransactionRepository transactionRepository,
            NotificationService notificationService) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Budget createBudget(User user, String name, BigDecimal amount, Category category,
                               String period, LocalDate startDate, LocalDate endDate, BigDecimal warningThreshold) {
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setName(name);
        budget.setAmount(amount);
        budget.setCategory(category);
        budget.setPeriod(period);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setWarningThreshold(warningThreshold);

        return budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public List<Budget> getUserBudgets(User user) {
        return budgetRepository.findByUser(user);
    }
    
    /**
     * Alias for getUserBudgets for consistency with other service methods
     */
    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByUser(User user) {
        return getUserBudgets(user);
    }

    @Transactional(readOnly = true)
    public List<Budget> getActiveBudgets(User user) {
        LocalDate today = LocalDate.now();
        return budgetRepository.findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user, today, today);
    }

    @Transactional(readOnly = true)
    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
    }

    @Transactional
    public Budget updateBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    /**
     * Gets the current spending for a budget within the specified period.
     * 
     * @param budget The budget to check spending for
     * @param accounts List of accounts to check transactions from
     * @return The current spending amount, or BigDecimal.ZERO if no transactions are found
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentSpending(Budget budget, List<Account> accounts) {
        // Early return if budget or accounts are invalid
        if (budget == null || budget.getCategory() == null || accounts == null || accounts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        LocalDateTime startDateTime;
        LocalDateTime endDateTime = LocalDateTime.now();

        try {
            switch (budget.getPeriod()) {
                case "DAILY":
                    startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
                    break;
                case "WEEKLY":
                    startDateTime = LocalDateTime.of(LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1), LocalTime.MIDNIGHT);
                    break;
                case "MONTHLY":
                    startDateTime = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIDNIGHT);
                    break;
                default:
                    // Fallback to budget start/end dates
                    if (budget.getStartDate() == null) {
                        // If no start date, default to 30 days ago
                        startDateTime = LocalDateTime.now().minusDays(30);
                    } else {
                        startDateTime = LocalDateTime.of(budget.getStartDate(), LocalTime.MIDNIGHT);
                    }
                    
                    if (budget.getEndDate() != null) {
                        endDateTime = LocalDateTime.of(budget.getEndDate(), LocalTime.MAX);
                    }
            }
            
            BigDecimal result = transactionRepository.getSumByAccountsAndCategoryAndDateBetween(
                    accounts, budget.getCategory(), startDateTime, endDateTime);
            
            // Return zero instead of null
            return result != null ? result : BigDecimal.ZERO;
            
        } catch (Exception e) {
            // Log the error but don't crash
            System.err.println("Error calculating budget spending: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    @Transactional
    public void checkBudgetThresholds(User user) {
        List<Budget> activeBudgets = getActiveBudgets(user);
        List<Account> userAccounts = user.getAccounts().stream().toList();

        for (Budget budget : activeBudgets) {
            try {
                // Skip invalid budgets
                if (budget.getAmount() == null || budget.getAmount().compareTo(BigDecimal.ZERO) <= 0 ||
                    budget.getWarningThreshold() == null) {
                    continue;
                }
                
                BigDecimal currentSpending = getCurrentSpending(budget, userAccounts);
                // Handle null spending
                if (currentSpending == null) {
                    currentSpending = BigDecimal.ZERO;
                }
                
                // Calculate percentage safely
                BigDecimal budgetPercentage = currentSpending.divide(budget.getAmount(), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal warningThreshold = budget.getWarningThreshold().divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                
                if (budgetPercentage.compareTo(warningThreshold) >= 0) {
                    notificationService.sendBudgetWarning(user, budget, currentSpending, budgetPercentage);
                }
            } catch (Exception e) {
                // Log but don't crash the entire notification process
                System.err.println("Error checking budget threshold for budget ID " + budget.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}