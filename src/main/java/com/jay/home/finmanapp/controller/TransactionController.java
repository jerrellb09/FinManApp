package com.jay.home.finmanapp.controller;

import com.jay.home.finmanapp.model.Account;
import com.jay.home.finmanapp.model.Category;
import com.jay.home.finmanapp.model.Transaction;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.service.AccountService;
import com.jay.home.finmanapp.service.CategoryService;
import com.jay.home.finmanapp.service.TransactionService;
import com.jay.home.finmanapp.service.UserService;
import com.jay.home.finmanapp.service.LoggingService;
import com.jay.home.finmanapp.util.TracingUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final LoggingService loggingService;

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
        this.loggingService = new LoggingService(TransactionController.class);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(
            @AuthenticationPrincipal String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long accountId) {

        Span span = TracingUtil.startSpan("transaction.list");
        try {
            loggingService.info("Retrieving transactions for user: {}, startDate: {}, endDate: {}, categoryId: {}, accountId: {}",
                    userEmail, startDate, endDate, categoryId, accountId);
            
            span.setTag("user.email", userEmail);
            if (startDate != null) span.setTag("date.start", startDate.toString());
            if (endDate != null) span.setTag("date.end", endDate.toString());
            if (categoryId != null) span.setTag("category.id", categoryId);
            if (accountId != null) span.setTag("account.id", accountId);
            
            User user = userService.getUserByEmail(userEmail);
            List<Account> accounts;

            if (accountId != null) {
                Account account = accountService.getAccountById(accountId);
                // Verify account belongs to user
                if (!account.getUser().getId().equals(user.getId())) {
                    loggingService.warn("Unauthorized access attempt: User {} tried to access account {}", 
                            userEmail, accountId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                accounts = List.of(account);
            } else {
                accounts = accountService.getUserAccounts(user);
            }

            span.setTag("accounts.count", accounts.size());
            loggingService.debug("Found {} accounts for user {}", accounts.size(), userEmail);

            Category category = null;
            if (categoryId != null) {
                category = categoryService.getCategoryById(categoryId);
                span.setTag("category.name", category.getName());
            }

            LocalDateTime startDateTime = startDate != null ?
                    LocalDateTime.of(startDate, LocalTime.MIN) :
                    LocalDateTime.of(LocalDate.now().minusMonths(1), LocalTime.MIN);

            LocalDateTime endDateTime = endDate != null ?
                    LocalDateTime.of(endDate, LocalTime.MAX) :
                    LocalDateTime.now();

            List<Transaction> transactions;
            if (category != null) {
                loggingService.debug("Retrieving transactions with category filter: {}", category.getName());
                transactions = transactionService.getTransactionsByAccountsAndCategoryAndDateBetween(
                        accounts, category, startDateTime, endDateTime);
            } else {
                loggingService.debug("Retrieving transactions without category filter");
                transactions = transactionService.getTransactionsByAccountsAndDateBetween(
                        accounts, startDateTime, endDateTime);
            }

            span.setTag("transactions.count", transactions.size());
            loggingService.info("Retrieved {} transactions for user {}", transactions.size(), userEmail);
            
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            loggingService.error("Error retrieving transactions", e);
            TracingUtil.recordException(e);
            throw e;
        } finally {
            span.finish();
        }
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
        Span span = TracingUtil.startSpan("transaction.sync");
        try {
            User user = userService.getUserByEmail(userEmail);
            List<Account> accounts = accountService.getUserAccounts(user);

            span.setTag("user.email", userEmail);
            span.setTag("accounts.count", accounts.size());
            
            int totalSynced = 0;
            for (Account account : accounts) {
                Span accountSpan = TracingUtil.startSpan("transaction.sync.account");
                try {
                    // Add account-specific tags
                    accountSpan.setTag("account.id", account.getId());
                    accountSpan.setTag("account.name", account.getName());
                    
                    int syncedCount = transactionService.syncTransactionsForAccount(account);
                    totalSynced += syncedCount;
                    
                    // Record result in the span
                    accountSpan.setTag("transactions.synced", syncedCount);
                } finally {
                    accountSpan.finish();
                }
            }

            // Record the total in the parent span
            span.setTag("transactions.total.synced", totalSynced);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "transactionsSynced", totalSynced
            ));
        } catch (Exception e) {
            // Record the exception in the current span
            TracingUtil.recordException(e);
            throw e;
        } finally {
            span.finish();
        }
    }
}