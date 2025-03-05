package com.jay.home.tradingbotv2.controller;

import com.jay.home.tradingbotv2.model.Budget;
import com.jay.home.tradingbotv2.model.Category;
import com.jay.home.tradingbotv2.model.User;
import com.jay.home.tradingbotv2.service.BudgetService;
import com.jay.home.tradingbotv2.service.CategoryService;
import com.jay.home.tradingbotv2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/budgets")

public class BudgetController {
    private final BudgetService budgetService;
    private final UserService userService;
    private final CategoryService categoryService;

    @Autowired
    public BudgetController(BudgetService budgetService, UserService userService, CategoryService categoryService) {
        this.budgetService = budgetService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getUserBudgets(@AuthenticationPrincipal String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        List<Budget> budgets = budgetService.getUserBudgets(user);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Budget>> getActiveBudgets(@AuthenticationPrincipal String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        List<Budget> activeBudgets = budgetService.getActiveBudgets(user);
        return ResponseEntity.ok(activeBudgets);
    }

    @PostMapping
    public ResponseEntity<Budget> createBudget(
            @AuthenticationPrincipal String userEmail,
            @Valid @RequestBody Map<String, Object> request) {
        User user = userService.getUserByEmail(userEmail);
        Category category = categoryService.getCategoryById(Long.valueOf(request.get("categoryId").toString()));

        Budget budget = budgetService.createBudget(
                user,
                (String) request.get("name"),
                new BigDecimal(request.get("amount").toString()),
                category,
                (String) request.get("period"),
                LocalDate.parse((String) request.get("startDate")),
                request.get("endDate") != null ? LocalDate.parse((String) request.get("endDate")) : null,
                new BigDecimal(request.get("warningThreshold").toString())
        );

        return new ResponseEntity<>(budget, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long id) {
        User user = userService.getUserByEmail(userEmail);
        Budget budget = budgetService.getBudgetById(id);

        // Verify the budget belongs to the authenticated user
        if (!budget.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(budget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long id,
            @Valid @RequestBody Map<String, Object> request) {
        User user = userService.getUserByEmail(userEmail);
        Budget budget = budgetService.getBudgetById(id);

        // Verify the budget belongs to the authenticated user
        if (!budget.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update fields if provided
        if (request.containsKey("name")) {
            budget.setName((String) request.get("name"));
        }

        if (request.containsKey("amount")) {
            budget.setAmount(new BigDecimal(request.get("amount").toString()));
        }

        if (request.containsKey("categoryId")) {
            Category category = categoryService.getCategoryById(Long.valueOf(request.get("categoryId").toString()));
            budget.setCategory(category);
        }

        if (request.containsKey("period")) {
            budget.setPeriod((String) request.get("period"));
        }

        if (request.containsKey("startDate")) {
            budget.setStartDate(LocalDate.parse((String) request.get("startDate")));
        }

        if (request.containsKey("endDate")) {
            budget.setEndDate(request.get("endDate") != null ?
                    LocalDate.parse((String) request.get("endDate")) : null);
        }

        if (request.containsKey("warningThreshold")) {
            budget.setWarningThreshold(new BigDecimal(request.get("warningThreshold").toString()));
        }

        Budget updatedBudget = budgetService.updateBudget(budget);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long id) {
        User user = userService.getUserByEmail(userEmail);
        Budget budget = budgetService.getBudgetById(id);

        // Verify the budget belongs to the authenticated user
        if (!budget.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        budgetService.deleteBudget(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/spending")
    public ResponseEntity<Map<String, Object>> getBudgetSpending(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long id) {
        User user = userService.getUserByEmail(userEmail);
        Budget budget = budgetService.getBudgetById(id);

        // Verify the budget belongs to the authenticated user
        if (!budget.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BigDecimal currentSpending = budgetService.getCurrentSpending(budget, user.getAccounts().stream().toList());
        BigDecimal percentage = currentSpending.divide(budget.getAmount(), 2).multiply(BigDecimal.valueOf(100));
        BigDecimal remaining = budget.getAmount().subtract(currentSpending);

        Map<String, Object> response = Map.of(
                "budgetId", budget.getId(),
                "budgetName", budget.getName(),
                "budgetAmount", budget.getAmount(),
                "currentSpending", currentSpending,
                "percentageUsed", percentage,
                "remaining", remaining
        );

        return ResponseEntity.ok(response);
    }
}