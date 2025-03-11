package com.jay.home.finmanapp.repository;

import com.jay.home.finmanapp.model.Account;
import com.jay.home.finmanapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    List<Account> findByUserAndType(User user, String type);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user = ?1 AND a.type = 'CHECKING' OR a.type = 'SAVINGS'")
    BigDecimal getTotalCashBalance(User user);
}