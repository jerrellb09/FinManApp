package com.jay.home.finmanapp.controller;


import com.jay.home.finmanapp.model.Account;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.service.AccountService;
import com.jay.home.finmanapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")

public class AccountController {
    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getUserAccounts(@AuthenticationPrincipal String userEmail) {
        System.out.println("AccountController.getUserAccounts called with userEmail: " + userEmail);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(null);
        }
        User user = userService.getUserByEmail(userEmail);
        return ResponseEntity.ok(accountService.getUserAccounts(user));
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, BigDecimal>> getCashBalance(@AuthenticationPrincipal String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        BigDecimal totalCash = accountService.getTotalCashBalance(user);
        return ResponseEntity.ok(Map.of("totalCash", totalCash));
    }

    // Link account with Plaid
    @PostMapping("/link")
    public ResponseEntity<Account> linkAccount(
            @AuthenticationPrincipal String userEmail,
            @RequestBody Map<String, String> request) {
        User user = userService.getUserByEmail(userEmail);
        Account account = accountService.linkAccount(
                user,
                request.get("publicToken"),
                request.get("institutionId"),
                request.get("institutionName")
        );
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountId}/sync")
    public ResponseEntity<Void> syncAccount(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long accountId) {
        // Get account and verify ownership
        // Sync with banking API
        return ResponseEntity.ok().build();
    }
}