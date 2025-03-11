package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.Account;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PlaidService plaidService;

    @Autowired
    public AccountService(AccountRepository accountRepository, PlaidService plaidService) {
        this.accountRepository = accountRepository;
        this.plaidService = plaidService;
    }

    @Transactional(readOnly = true)
    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUser(user);
    }
    
    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCashBalance(User user) {
        return accountRepository.getTotalCashBalance(user);
    }

    @Transactional
    public Account linkAccount(User user, String publicToken, String institutionId, String institutionName) {
        // Exchange public token for access token via Plaid API
        String accessToken = plaidService.exchangePublicToken(publicToken);

        // Get account details from Plaid
        var accountDetails = plaidService.getAccountDetails(accessToken);

        // Create new account
        Account account = new Account();
        account.setUser(user);
        account.setName(accountDetails.getName());
        account.setType(accountDetails.getType());
        account.setBalance(accountDetails.getBalance());
        account.setAccountId(accountDetails.getAccountId());
        account.setAccessToken(accessToken);
        account.setInstitutionId(institutionId);
        account.setInstitutionName(institutionName);
        account.setLastSynced(LocalDateTime.now());

        return accountRepository.save(account);
    }

    @Transactional
    public void syncAccount(Account account) {
        var updatedDetails = plaidService.getAccountDetails(account.getAccessToken());
        account.setBalance(updatedDetails.getBalance());
        account.setLastSynced(LocalDateTime.now());
        accountRepository.save(account);
    }
}