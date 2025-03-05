package com.jay.home.tradingbotv2.controller;

import com.jay.home.tradingbotv2.model.Account;
import com.jay.home.tradingbotv2.model.Category;
import com.jay.home.tradingbotv2.model.Transaction;
import com.jay.home.tradingbotv2.model.User;
import com.jay.home.tradingbotv2.service.AccountService;
import com.jay.home.tradingbotv2.service.CategoryService;
import com.jay.home.tradingbotv2.service.TransactionService;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")

public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;
    private final AccountService accountService;
    private final CategoryService categoryService;

    @Autowired
    public TransactionController(
            TransactionService transactionService,
            UserService userService,
            AccountService accountService,
            CategoryService categoryService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(
            @AuthenticationPrincipal String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long accountId) {

        User user = userService.getUserByEmail(userEmail);
        List<Account> accounts;

        if (accountId != null) {
            Account account = accountService.getAccountById(accountId);
            // Verify account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            accounts = List.of(account);
        } else {
            accounts = accountService.getUserAccounts(user);
        }

        Category category = null;
        if (categoryId != null) {
            category = categoryService.getCategoryById(categoryId);
        }

        LocalDateTime startDateTime = startDate != null ?
                LocalDateTime.of(startDate, LocalTime.MIN) :
                LocalDateTime.of(LocalDate.now().minusMonths(1), LocalTime.MIN);

        LocalDateTime endDateTime = endDate != null ?
                LocalDateTime.of(endDate, LocalTime.MAX) :
                LocalDateTime.now();

        List<Transaction> transactions;
        if (category != null) {
            transactions = transactionService.getTransactionsByAccountsAndCategoryAndDateBetween(
                    accounts, category, startDateTime, endDateTime);
        } else {
            transactions = transactionService.getTransactionsByAccountsAndDateBetween(
                    accounts, startDateTime, endDateTime);
        }

        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<Transaction> addManualTransaction(
            @AuthenticationPrincipal String userEmail,
            @Valid @RequestBody Map<String, Object> request) {

        User user = userService.getUserByEmail(userEmail);
        Long accountId = Long.valueOf(request.get("accountId").toString());
        Account account = accountService.getAccountById(accountId);

        // Verify account belongs to user
        if (!account.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Category category = null;
        if (request.containsKey("categoryId") && request.get("categoryId") != null) {
            Long categoryId = Long.valueOf(request.get("categoryId").toString());
            category = categoryService.getCategoryById(categoryId);
        }

        LocalDateTime transactionDate = request.containsKey("date") && request.get("date") != null ?
                LocalDateTime.parse((String) request.get("date")) :
                LocalDateTime.now();

        Transaction transaction = transactionService.addManualTransaction(
                account,
                (String) request.get("description"),
                new BigDecimal(request.get("amount").toString()),
                transactionDate,
                category
        );

        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long id,
            @Valid @RequestBody Map<String, Object> request) {

        User user = userService.getUserByEmail(userEmail);
        Transaction transaction = transactionService.getTransactionById(id);

        // Verify transaction belongs to user's account
        if (!transaction.getAccount().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Only allow updating manual transactions
        if (!transaction.isManualEntry()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        if (request.containsKey("description")) {
            transaction.setDescription((String) request.get("description"));
        }

        if (request.containsKey("amount")) {
            transaction.setAmount(new BigDecimal(request.get("amount").toString()));
        }

        if (request.containsKey("date")) {
            transaction.setDate(LocalDateTime.parse((String) request.get("date")));
        }

        if (request.containsKey("categoryId")) {
            Category category = request.get("categoryId") != null ?
                    categoryService.getCategoryById(Long.valueOf(request.get("categoryId").toString())) :
                    null;
            transaction.setCategory(category);
        }

        Transaction updatedTransaction = transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long id) {

        User user = userService.getUserByEmail(userEmail);
        Transaction transaction = transactionService.getTransactionById(id);

        // Verify transaction belongs to user's account
        if (!transaction.getAccount().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Only allow deleting manual transactions
        if (!transaction.isManualEntry()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        transactionService.deleteTransaction(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/categorize/{id}")
    public ResponseEntity<Transaction> categorizeTransaction(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {

        User user = userService.getUserByEmail(userEmail);
        Transaction transaction = transactionService.getTransactionById(id);

        // Verify transaction belongs to user's account
        if (!transaction.getAccount().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Category category = categoryService.getCategoryById(request.get("categoryId"));
        transaction.setCategory(category);

        Transaction updatedTransaction = transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncTransactions(@AuthenticationPrincipal String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        List<Account> accounts = accountService.getUserAccounts(user);

        int totalSynced = 0;
        for (Account account : accounts) {
            int syncedCount = transactionService.syncTransactionsForAccount(account);
            totalSynced += syncedCount;
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "transactionsSynced", totalSynced
        ));
    }
}