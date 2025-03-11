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

    @Transactional(readOnly = true)
    public BigDecimal getCurrentSpending(Budget budget, List<Account> accounts) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime = LocalDateTime.now();

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
                startDateTime = LocalDateTime.of(budget.getStartDate(), LocalTime.MIDNIGHT);
                if (budget.getEndDate() != null) {
                    endDateTime = LocalDateTime.of(budget.getEndDate(), LocalTime.MAX);
                }
        }

        return transactionRepository.getSumByAccountsAndCategoryAndDateBetween(
                accounts, budget.getCategory(), startDateTime, endDateTime);
    }

    @Transactional
    public void checkBudgetThresholds(User user) {
        List<Budget> activeBudgets = getActiveBudgets(user);
        List<Account> userAccounts = user.getAccounts().stream().toList();

        for (Budget budget : activeBudgets) {
            BigDecimal currentSpending = getCurrentSpending(budget, userAccounts);
            BigDecimal budgetPercentage = currentSpending.divide(budget.getAmount(), 2);

            if (budgetPercentage.compareTo(budget.getWarningThreshold().divide(BigDecimal.valueOf(100))) >= 0) {
                notificationService.sendBudgetWarning(user, budget, currentSpending, budgetPercentage);
            }
        }
    }
}