package com.jay.home.finmanapp.controller;

import com.jay.home.finmanapp.model.Account;
import com.jay.home.finmanapp.model.Category;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.service.AccountService;
import com.jay.home.finmanapp.service.CategoryService;
import com.jay.home.finmanapp.service.InsightService;
import com.jay.home.finmanapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insights")

public class InsightController {
    private final InsightService insightService;
    private final UserService userService;
    private final AccountService accountService;
    private final CategoryService categoryService;

    @Autowired
    public InsightController(
            InsightService insightService,
            UserService userService,
            AccountService accountService,
            CategoryService categoryService) {
        this.insightService = insightService;
        this.userService = userService;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    @GetMapping("/spending-by-category")
    public ResponseEntity<List<Map<String, Object>>> getSpendingByCategory(
            @AuthenticationPrincipal String userEmail,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        User user = userService.getUserByEmail(userEmail);
        List<Account> accounts = accountService.getUserAccounts(user);
        List<Map<String, Object>> categorySpending = insightService.getSpendingByCategory(accounts, startDate, endDate);
        return ResponseEntity.ok(categorySpending);
    }

    @GetMapping("/spending-trend")
    public ResponseEntity<List<Map<String, Object>>> getSpendingTrend(
            @AuthenticationPrincipal String userEmail,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String period) { // daily, weekly, monthly

        User user = userService.getUserByEmail(userEmail);
        List<Account> accounts = accountService.getUserAccounts(user);
        String timePeriod = period != null ? period.toUpperCase() : "MONTHLY";
        List<Map<String, Object>> spendingTrend = insightService.getSpendingTrend(accounts, startDate, endDate, timePeriod);
        return ResponseEntity.ok(spendingTrend);
    }

    @GetMapping("/category-trend/{categoryId}")
    public ResponseEntity<List<Map<String, Object>>> getCategoryTrend(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String period) { // daily, weekly, monthly

        User user = userService.getUserByEmail(userEmail);
        List<Account> accounts = accountService.getUserAccounts(user);
        Category category = categoryService.getCategoryById(categoryId);

        String timePeriod = period != null ? period.toUpperCase() : "MONTHLY";
        List<Map<String, Object>> categoryTrend = insightService.getCategoryTrend(
                accounts, category, startDate, endDate, timePeriod);
        return ResponseEntity.ok(categoryTrend);
    }

    @GetMapping("/budget-performance")
    public ResponseEntity<List<Map<String, Object>>> getBudgetPerformance(
            @AuthenticationPrincipal String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        User user = userService.getUserByEmail(userEmail);
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(3);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<Map<String, Object>> budgetPerformance = insightService.getBudgetPerformance(user, start, end);
        return ResponseEntity.ok(budgetPerformance);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(
            @AuthenticationPrincipal String userEmail,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        User user = userService.getUserByEmail(userEmail);
        LocalDate date;

        if (year != null && month != null) {
            date = LocalDate.of(year, month, 1);
        } else {
            date = LocalDate.now().withDayOfMonth(1);
        }

        Map<String, Object> summary = insightService.getMonthlySummary(user, date.getYear(), date.getMonthValue());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/suggested-budgets")
    public ResponseEntity<List<Map<String, Object>>> getSuggestedBudgets(
            @AuthenticationPrincipal String userEmail) {

        User user = userService.getUserByEmail(userEmail);
        List<Map<String, Object>> suggestions = insightService.generateBudgetSuggestions(user);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/top-merchants")
    public ResponseEntity<List<Map<String, Object>>> getTopMerchants(
            @AuthenticationPrincipal String userEmail,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {

        User user = userService.getUserByEmail(userEmail);
        List<Account> accounts = accountService.getUserAccounts(user);
        List<Map<String, Object>> topMerchants = insightService.getTopMerchants(accounts, startDate, endDate, limit);
        return ResponseEntity.ok(topMerchants);
    }
    
    @GetMapping("/bills-vs-income")
    public ResponseEntity<Map<String, Object>> getBillsVsIncome(@AuthenticationPrincipal String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        Map<String, Object> billsVsIncome = insightService.getBillsVsIncomeInsight(user.getId());
        return ResponseEntity.ok(billsVsIncome);
    }
}