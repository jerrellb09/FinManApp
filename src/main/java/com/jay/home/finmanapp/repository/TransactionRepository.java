package com.jay.home.finmanapp.repository;

import com.jay.home.finmanapp.model.Account;
import com.jay.home.finmanapp.model.Category;
import com.jay.home.finmanapp.model.Transaction;
import com.jay.home.finmanapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);
    List<Transaction> findByAccountIn(List<Account> accounts);
    List<Transaction> findByAccountInAndDateBetween(
            List<Account> accounts, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByAccountInAndCategory(List<Account> accounts, Category category);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.user = ?1")
    List<Transaction> findByAccountUser(User user);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.user = ?1 AND t.date > ?2")
    List<Transaction> findByAccountUserAndDateAfter(User user, LocalDateTime date);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account IN ?1 AND t.category = ?2 AND t.date BETWEEN ?3 AND ?4")
    BigDecimal getSumByAccountsAndCategoryAndDateBetween(
            List<Account> accounts, Category category, LocalDateTime startDate, LocalDateTime endDate);
}