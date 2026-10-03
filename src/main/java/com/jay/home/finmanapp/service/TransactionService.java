package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.Account;
import com.jay.home.finmanapp.model.Category;
import com.jay.home.finmanapp.model.Transaction;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    
    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepository;
    
    public List<Transaction> getTransactionsByAccountsAndCategoryAndDateBetween(
            List<Account> accounts, Category category, LocalDateTime startDate, LocalDateTime endDate) {
        if (accounts.isEmpty()) {
            return List.of();
        }
        return transactionRepository.findByAccountInAndCategoryAndDateBetween(accounts, category, startDate, endDate);
    }
    
    public List<Transaction> getTransactionsByAccountsAndDateBetween(
            List<Account> accounts, LocalDateTime startDate, LocalDateTime endDate) {
        if (accounts.isEmpty()) {
            return List.of();
        }
        return transactionRepository.findByAccountInAndDateBetween(accounts, startDate, endDate);
    }
    
    /**
     * Get recent transactions for a user within the specified number of days
     * @param user The user to get transactions for
     * @param days Number of days to look back
     * @return List of transactions
     */
    public List<Transaction> getRecentTransactionsForUser(User user, int days) {
        List<Account> accounts = accountService.getUserAccounts(user);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        return getTransactionsByAccountsAndDateBetween(accounts, startDate, endDate);
    }
    
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }
    
    public Transaction addManualTransaction(
            Account account, String description, BigDecimal amount, 
            LocalDateTime date, Category category) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionId("manual-" + UUID.randomUUID());
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setCategory(category);
        transaction.setManualEntry(true);
        return transactionRepository.save(transaction);
    }
    
    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
    
    public int syncTransactionsForAccount(Account account) {
        // Implementation placeholder
        return 0;
    }
}