package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.dto.TransactionImportDTO;
import com.jay.home.finmanapp.model.*;
import com.jay.home.finmanapp.repository.AccountRepository;
import com.jay.home.finmanapp.repository.CategoryRepository;
import com.jay.home.finmanapp.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSVIngestionService {

    private final CSVParserService csvParserService;
    private final TransactionAnalyzerService transactionAnalyzerService;
    private final BillService billService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CSVImportResult importCSV(MultipartFile file, User user) throws IOException {
        log.info("Starting CSV import for user: {}", user.getEmail());
        
        CSVImportResult result = new CSVImportResult();
        
        try {
            // Parse CSV file
            List<TransactionImportDTO> importedTransactions = csvParserService.parseCSV(file);
            result.setTotalTransactions(importedTransactions.size());
            
            // Create or find accounts
            Map<String, Account> accountMap = createOrFindAccounts(importedTransactions, user);
            result.setAccountsCreated(accountMap.size());
            
            // Create or find categories
            Map<String, Category> categoryMap = createOrFindCategories(importedTransactions);
            result.setCategoriesCreated(categoryMap.size());
            
            // Save transactions
            List<Transaction> savedTransactions = saveTransactions(importedTransactions, accountMap, categoryMap);
            result.setTransactionsSaved(savedTransactions.size());
            
            // Analyze for recurring patterns
            List<TransactionAnalyzerService.RecurringTransactionGroup> recurringGroups = 
                transactionAnalyzerService.identifyRecurringTransactions(importedTransactions);
            
            // Create bills from recurring transactions
            List<Bill> createdBills = createBillsFromRecurringTransactions(recurringGroups, user, categoryMap);
            result.setBillsCreated(createdBills.size());
            
            log.info("CSV import completed successfully. {} transactions, {} bills created", 
                    savedTransactions.size(), createdBills.size());
            
        } catch (Exception e) {
            log.error("CSV import failed for user: {}", user.getEmail(), e);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    private Map<String, Account> createOrFindAccounts(List<TransactionImportDTO> transactions, User user) {
        return transactions.stream()
                .filter(t -> t.getAccountName() != null && !t.getAccountName().trim().isEmpty())
                .collect(Collectors.toMap(
                    t -> t.getAccountName() + "_" + t.getInstitutionName(),
                    t -> createOrFindAccount(t, user),
                    (existing, replacement) -> existing
                ));
    }
    
    private Account createOrFindAccount(TransactionImportDTO transaction, User user) {
        String accountKey = transaction.getAccountName() + "_" + transaction.getInstitutionName();
        
        Optional<Account> existingAccount = accountRepository.findByNameAndUser(transaction.getAccountName(), user);
        
        if (existingAccount.isPresent()) {
            return existingAccount.get();
        }
        
        Account account = new Account();
        account.setName(transaction.getAccountName());
        account.setType(mapAccountType(transaction.getAccountType()));
        account.setInstitutionName(transaction.getInstitutionName());
        account.setAccountId(transaction.getAccountNumber());
        account.setUser(user);
        account.setBalance(java.math.BigDecimal.ZERO);
        account.setLastSynced(LocalDateTime.now());
        account.setAccessToken("CSV_IMPORT_" + System.currentTimeMillis());
        account.setInstitutionId(transaction.getInstitutionName() != null ? 
            transaction.getInstitutionName().replaceAll("\\s+", "_").toUpperCase() : "UNKNOWN");
        
        return accountRepository.save(account);
    }
    
    private String mapAccountType(String csvAccountType) {
        if (csvAccountType == null) return "CHECKING";
        
        switch (csvAccountType.toUpperCase()) {
            case "CASH":
            case "CHECKING":
                return "CHECKING";
            case "SAVINGS":
                return "SAVINGS";
            case "CREDIT CARD":
                return "CREDIT";
            case "INVESTMENT":
                return "INVESTMENT";
            default:
                return "CHECKING";
        }
    }
    
    private Map<String, Category> createOrFindCategories(List<TransactionImportDTO> transactions) {
        return transactions.stream()
                .filter(t -> t.getCategory() != null && !t.getCategory().trim().isEmpty())
                .collect(Collectors.toMap(
                    TransactionImportDTO::getCategory,
                    this::createOrFindCategory,
                    (existing, replacement) -> existing
                ));
    }
    
    private Category createOrFindCategory(TransactionImportDTO transaction) {
        String categoryName = transaction.getCategory();
        
        Optional<Category> existingCategory = categoryRepository.findByName(categoryName);
        
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }
        
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription("Auto-created from CSV import");
        
        return categoryRepository.save(category);
    }
    
    private List<Transaction> saveTransactions(List<TransactionImportDTO> importedTransactions, 
                                             Map<String, Account> accountMap, 
                                             Map<String, Category> categoryMap) {
        List<Transaction> transactions = new ArrayList<>();
        
        for (TransactionImportDTO importDto : importedTransactions) {
            Transaction transaction = new Transaction();
            
            transaction.setTransactionId(importDto.getDisplayName() + "_" + importDto.getDate().toString());
            transaction.setDescription(importDto.getDescription());
            transaction.setAmount(importDto.getAmount());
            transaction.setDate(importDto.getDate().atStartOfDay());
            transaction.setManualEntry(false);
            
            // Find account - account is required for transactions
            String accountKey = importDto.getAccountName() + "_" + importDto.getInstitutionName();
            Account account = accountMap.get(accountKey);
            if (account != null) {
                transaction.setAccount(account);
            } else {
                // Skip transactions without valid accounts to avoid constraint violations
                log.warn("Skipping transaction - no account found for: {}", accountKey);
                continue;
            }
            
            // Find category
            if (importDto.getCategory() != null) {
                Category category = categoryMap.get(importDto.getCategory());
                transaction.setCategory(category);
            }
            
            transactions.add(transaction);
        }
        
        return transactionRepository.saveAll(transactions);
    }
    
    private List<Bill> createBillsFromRecurringTransactions(
            List<TransactionAnalyzerService.RecurringTransactionGroup> recurringGroups, 
            User user, 
            Map<String, Category> categoryMap) {
        
        List<Bill> bills = new ArrayList<>();
        
        for (TransactionAnalyzerService.RecurringTransactionGroup group : recurringGroups) {
            if (!group.isBill()) {
                continue; // Skip non-bill transactions
            }
            
            Bill bill = new Bill();
            bill.setName(group.getMerchantName());
            bill.setAmount(group.getAverageAmount());
            bill.setRecurring(true);
            bill.setRecurringPeriod(mapRecurringPeriod(group.getPeriod()));
            bill.setAutoPay(false);
            bill.setPaid(false);
            bill.setDescription("Auto-created from CSV import");
            bill.setUser(user);
            
            // Set due day based on most recent transaction
            if (!group.getTransactions().isEmpty()) {
                TransactionImportDTO lastTransaction = group.getTransactions().get(group.getTransactions().size() - 1);
                if (lastTransaction.getDate() != null) {
                    bill.setDueDay(lastTransaction.getDate().getDayOfMonth());
                }
            }
            
            // Set category
            if (group.getCategory() != null) {
                Category category = categoryMap.get(group.getCategory());
                bill.setCategory(category);
            }
            
            bills.add(bill);
        }
        
        return billService.saveAll(bills);
    }
    
    private String mapRecurringPeriod(TransactionAnalyzerService.RecurringPeriod period) {
        switch (period) {
            case WEEKLY:
                return "WEEKLY";
            case BIWEEKLY:
                return "BIWEEKLY";
            case MONTHLY:
                return "MONTHLY";
            case QUARTERLY:
                return "QUARTERLY";
            case ANNUALLY:
                return "ANNUALLY";
            default:
                return "MONTHLY";
        }
    }
    
    public static class CSVImportResult {
        private int totalTransactions;
        private int transactionsSaved;
        private int accountsCreated;
        private int categoriesCreated;
        private int billsCreated;
        private String errorMessage;
        
        // Getters and setters
        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public int getTransactionsSaved() { return transactionsSaved; }
        public void setTransactionsSaved(int transactionsSaved) { this.transactionsSaved = transactionsSaved; }
        
        public int getAccountsCreated() { return accountsCreated; }
        public void setAccountsCreated(int accountsCreated) { this.accountsCreated = accountsCreated; }
        
        public int getCategoriesCreated() { return categoriesCreated; }
        public void setCategoriesCreated(int categoriesCreated) { this.categoriesCreated = categoriesCreated; }
        
        public int getBillsCreated() { return billsCreated; }
        public void setBillsCreated(int billsCreated) { this.billsCreated = billsCreated; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public boolean isSuccess() { return errorMessage == null; }
    }
}