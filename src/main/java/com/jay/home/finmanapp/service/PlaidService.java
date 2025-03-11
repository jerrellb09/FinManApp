package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.PlaidClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaidService {
    private final PlaidClient plaidClient;

    @Autowired
    public PlaidService(PlaidClient plaidClient) {
        this.plaidClient = plaidClient;
    }

    public String exchangePublicToken(String publicToken) {
        var response = plaidClient.itemPublicTokenExchange()
                .execute(publicToken)
                .getBody();

        return response.getAccessToken();
    }

    public AccountDetails getAccountDetails(String accessToken) {
        var response = plaidClient.accountsGet()
                .execute(accessToken)
                .getBody();

        var account = response.getAccounts().get(0);

        return new AccountDetails(
                account.getAccountId(),
                account.getName(),
                account.getType().getValue(),
                account.getBalances().getCurrent()
        );
    }

    public List<TransactionDetails> getTransactions(String accessToken, LocalDate startDate, LocalDate endDate) {
        var response = plaidClient.transactionsGet()
                .execute(accessToken)
                .getBody();

        return response.getTransactions().stream()
                .map(transaction -> new TransactionDetails(
                        transaction.getTransactionId(),
                        transaction.getName(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getCategory()))
                .collect(Collectors.toList());
    }

    // Inner classes for encapsulating Plaid API responses
    public static class AccountDetails {
        private final String accountId;
        private final String name;
        private final String type;
        private final BigDecimal balance;

        public AccountDetails(String accountId, String name, String type, BigDecimal balance) {
            this.accountId = accountId;
            this.name = name;
            this.type = type;
            this.balance = balance;
        }

        public String getAccountId() {
            return accountId;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }

    public static class TransactionDetails {
        private final String transactionId;
        private final String description;
        private final BigDecimal amount;
        private final LocalDate date;
        private final List<String> categories;

        public TransactionDetails(String transactionId, String description, BigDecimal amount, 
                                 LocalDate date, List<String> categories) {
            this.transactionId = transactionId;
            this.description = description;
            this.amount = amount;
            this.date = date;
            this.categories = categories;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public LocalDate getDate() {
            return date;
        }

        public List<String> getCategories() {
            return categories;
        }
    }
}