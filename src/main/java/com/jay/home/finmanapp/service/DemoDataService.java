package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.*;
import com.jay.home.finmanapp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for creating and managing demo data.
 * This service populates the demo user account with sample data
 * to showcase the application's functionality.
 */
@Service
public class DemoDataService {
    private static final Logger logger = LoggerFactory.getLogger(DemoDataService.class);
    
    private final UserService userService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final BillRepository billRepository;
    
    @Autowired
    public DemoDataService(
            UserService userService,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            BudgetRepository budgetRepository,
            BillRepository billRepository) {
        this.userService = userService;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.billRepository = billRepository;
    }
    
    /**
     * Initialize demo data for the specified user.
     * Creates sample accounts, transactions, budgets, and bills.
     * 
     * @param user The user to create demo data for
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean initializeDemoData(User user) {
        try {
            logger.info("Initializing demo data for user: {}", user.getEmail());
            
            // Create categories first if they don't exist
            Map<String, Category> categories = ensureCategories();
            
            // Create demo accounts
            List<Account> accounts = createDemoAccounts(user);
            
            // Create demo transactions
            createDemoTransactions(user, accounts, categories);
            
            // Create demo budgets
            createDemoBudgets(user, categories);
            
            // Create demo bills
            createDemoBills(user);
            
            logger.info("Demo data initialization completed successfully");
            return true;
        } catch (Exception e) {
            logger.error("Error initializing demo data: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Ensure essential categories exist in the database.
     * 
     * @return Map of category names to Category objects
     */
    private Map<String, Category> ensureCategories() {
        Map<String, Category> categoriesMap = new HashMap<>();
        String[] categoryNames = {
            "Housing", "Utilities", "Food", "Transportation", "Entertainment", 
            "Shopping", "Health", "Education", "Travel", "Income"
        };
        
        for (String name : categoryNames) {
            Category category = categoryRepository.findByName(name).orElse(null);
            if (category == null) {
                category = new Category();
                category.setName(name);
                category.setDescription("Category for " + name.toLowerCase() + " expenses");
                category = categoryRepository.save(category);
                logger.info("Created category: {}", name);
            }
            categoriesMap.put(name, category);
        }
        
        return categoriesMap;
    }
    
    /**
     * Create demo accounts for the user.
     * 
     * @param user The user to create accounts for
     * @return List of created accounts
     */
    private List<Account> createDemoAccounts(User user) {
        // First clear any existing accounts
        List<Account> existingAccounts = accountRepository.findByUser(user);
        if (!existingAccounts.isEmpty()) {
            logger.info("Removing {} existing accounts", existingAccounts.size());
            accountRepository.deleteAll(existingAccounts);
        }
        
        // Create checking account
        Account checkingAccount = new Account();
        checkingAccount.setUser(user);
        checkingAccount.setName("Demo Checking");
        checkingAccount.setType("CHECKING");
        checkingAccount.setBalance(new BigDecimal("3250.75"));
        checkingAccount.setAccountId("demo-checking-" + UUID.randomUUID());
        checkingAccount.setAccessToken("demo-access-token");
        checkingAccount.setInstitutionId("demo-bank");
        checkingAccount.setInstitutionName("Demo Bank");
        checkingAccount.setLastSynced(LocalDateTime.now());
        
        // Create savings account
        Account savingsAccount = new Account();
        savingsAccount.setUser(user);
        savingsAccount.setName("Demo Savings");
        savingsAccount.setType("SAVINGS");
        savingsAccount.setBalance(new BigDecimal("12500.00"));
        savingsAccount.setAccountId("demo-savings-" + UUID.randomUUID());
        savingsAccount.setAccessToken("demo-access-token");
        savingsAccount.setInstitutionId("demo-bank");
        savingsAccount.setInstitutionName("Demo Bank");
        savingsAccount.setLastSynced(LocalDateTime.now());
        
        // Create credit card account
        Account creditAccount = new Account();
        creditAccount.setUser(user);
        creditAccount.setName("Demo Credit Card");
        creditAccount.setType("CREDIT");
        creditAccount.setBalance(new BigDecimal("-1250.50"));
        creditAccount.setAccountId("demo-credit-" + UUID.randomUUID());
        creditAccount.setAccessToken("demo-access-token");
        creditAccount.setInstitutionId("demo-credit");
        creditAccount.setInstitutionName("Demo Credit Union");
        creditAccount.setLastSynced(LocalDateTime.now());
        
        // Save accounts
        List<Account> accounts = Arrays.asList(
            accountRepository.save(checkingAccount),
            accountRepository.save(savingsAccount),
            accountRepository.save(creditAccount)
        );
        
        logger.info("Created {} demo accounts", accounts.size());
        return accounts;
    }
    
    /**
     * Create demo transactions for the user's accounts.
     * 
     * @param user The user to create transactions for
     * @param accounts The user's accounts
     * @param categories Map of category names to Category objects
     */
    private void createDemoTransactions(User user, List<Account> accounts, Map<String, Category> categories) {
        // First clear any existing transactions
        for (Account account : accounts) {
            List<Transaction> existingTransactions = transactionRepository.findByAccount(account);
            if (!existingTransactions.isEmpty()) {
                logger.info("Removing {} existing transactions for account {}", 
                    existingTransactions.size(), account.getName());
                transactionRepository.deleteAll(existingTransactions);
            }
        }
        
        Account checkingAccount = accounts.get(0);
        Account savingsAccount = accounts.get(1);
        Account creditAccount = accounts.get(2);
        
        List<Transaction> transactions = new ArrayList<>();
        
        // Create transactions for the past 3 months
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeMonthsAgo = now.minusMonths(3);
        
        // Income transactions - monthly salary
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(15);
        for (int i = 0; i < 3; i++) {
            LocalDate payDate = currentMonth.minusMonths(i);
            Transaction salaryTransaction = new Transaction();
            salaryTransaction.setAccount(checkingAccount);
            salaryTransaction.setTransactionId("demo-salary-" + UUID.randomUUID());
            salaryTransaction.setDescription("PAYROLL DEPOSIT - DEMO COMPANY");
            salaryTransaction.setAmount(new BigDecimal("5000.00"));
            salaryTransaction.setDate(LocalDateTime.of(payDate, LocalTime.of(8, 30)));
            salaryTransaction.setCategory(categories.get("Income"));
            salaryTransaction.setManualEntry(false);
            transactions.add(salaryTransaction);
        }
        
        // Recurring monthly expenses
        String[] recurringExpenses = {
            "RENT PAYMENT|Housing|1500.00", 
            "NETFLIX SUBSCRIPTION|Entertainment|14.99",
            "SPOTIFY PREMIUM|Entertainment|9.99",
            "WATER UTILITY|Utilities|85.75",
            "POWER ELECTRIC|Utilities|125.50",
            "INTERNET SERVICE|Utilities|79.99",
            "PHONE BILL|Utilities|95.00",
            "GYM MEMBERSHIP|Health|50.00"
        };
        
        for (int month = 0; month < 3; month++) {
            for (String expense : recurringExpenses) {
                String[] parts = expense.split("\\|");
                String description = parts[0];
                String categoryName = parts[1];
                BigDecimal amount = new BigDecimal(parts[2]).negate();
                
                LocalDate expenseDate = LocalDate.now()
                    .minusMonths(month)
                    .withDayOfMonth(ThreadLocalRandom.current().nextInt(1, 28));
                
                Transaction transaction = new Transaction();
                transaction.setAccount(checkingAccount);
                transaction.setTransactionId("demo-expense-" + UUID.randomUUID());
                transaction.setDescription(description);
                transaction.setAmount(amount);
                transaction.setDate(LocalDateTime.of(expenseDate, LocalTime.of(
                    ThreadLocalRandom.current().nextInt(8, 20),
                    ThreadLocalRandom.current().nextInt(0, 59))));
                transaction.setCategory(categories.get(categoryName));
                transaction.setManualEntry(false);
                transactions.add(transaction);
            }
        }
        
        // Random food transactions
        String[] foodMerchants = {
            "GRUBHUB|35.75", "UBER EATS|42.99", "DOORDASH|28.50",
            "WHOLE FOODS|125.35", "TRADER JOE'S|85.45", "SAFEWAY|65.75",
            "STARBUCKS|5.25", "MCDONALD'S|12.50", "CHIPOTLE|18.75",
            "LOCAL RESTAURANT|65.50", "FOOD TRUCK|15.00", "DINER|22.50"
        };
        
        for (int i = 0; i < 40; i++) {
            String[] parts = foodMerchants[ThreadLocalRandom.current().nextInt(0, foodMerchants.length)].split("\\|");
            String merchant = parts[0];
            BigDecimal baseAmount = new BigDecimal(parts[1]);
            
            // Vary the amount a bit for realism
            BigDecimal variation = new BigDecimal(String.format("%.2f", ThreadLocalRandom.current().nextDouble(-5, 5)));
            BigDecimal amount = baseAmount.add(variation).negate();
            
            LocalDateTime transactionDate = threeMonthsAgo.plusDays(
                ThreadLocalRandom.current().nextInt(0, 90));
            
            Transaction transaction = new Transaction();
            transaction.setAccount(ThreadLocalRandom.current().nextBoolean() ? checkingAccount : creditAccount);
            transaction.setTransactionId("demo-food-" + UUID.randomUUID());
            transaction.setDescription(merchant);
            transaction.setAmount(amount);
            transaction.setDate(transactionDate);
            transaction.setCategory(categories.get("Food"));
            transaction.setManualEntry(false);
            transactions.add(transaction);
        }
        
        // Transportation transactions
        String[] transportationMerchants = {
            "UBER|25.50", "LYFT|22.75", "SHELL GAS|45.50", 
            "BP GAS STATION|40.25", "TRANSIT AUTHORITY|5.00", 
            "PARKING GARAGE|15.00", "CAR WASH|12.00", "AUTO REPAIR|325.50"
        };
        
        for (int i = 0; i < 20; i++) {
            String[] parts = transportationMerchants[ThreadLocalRandom.current().nextInt(0, transportationMerchants.length)].split("\\|");
            String merchant = parts[0];
            BigDecimal baseAmount = new BigDecimal(parts[1]);
            
            // Vary the amount a bit for realism
            BigDecimal variation = new BigDecimal(String.format("%.2f", ThreadLocalRandom.current().nextDouble(-3, 5)));
            BigDecimal amount = baseAmount.add(variation).negate();
            
            LocalDateTime transactionDate = threeMonthsAgo.plusDays(
                ThreadLocalRandom.current().nextInt(0, 90));
            
            Transaction transaction = new Transaction();
            transaction.setAccount(ThreadLocalRandom.current().nextBoolean() ? checkingAccount : creditAccount);
            transaction.setTransactionId("demo-transport-" + UUID.randomUUID());
            transaction.setDescription(merchant);
            transaction.setAmount(amount);
            transaction.setDate(transactionDate);
            transaction.setCategory(categories.get("Transportation"));
            transaction.setManualEntry(false);
            transactions.add(transaction);
        }
        
        // Shopping transactions
        String[] shoppingMerchants = {
            "AMAZON.COM|75.25", "TARGET|120.50", "WALMART|95.75", 
            "BEST BUY|250.00", "APPLE|129.99", "MACY'S|85.50", 
            "NORDSTROM|165.75", "HOME DEPOT|145.25", "IKEA|225.00"
        };
        
        for (int i = 0; i < 15; i++) {
            String[] parts = shoppingMerchants[ThreadLocalRandom.current().nextInt(0, shoppingMerchants.length)].split("\\|");
            String merchant = parts[0];
            BigDecimal baseAmount = new BigDecimal(parts[1]);
            
            // Vary the amount a bit for realism
            BigDecimal variation = new BigDecimal(String.format("%.2f", ThreadLocalRandom.current().nextDouble(-10, 15)));
            BigDecimal amount = baseAmount.add(variation).negate();
            
            LocalDateTime transactionDate = threeMonthsAgo.plusDays(
                ThreadLocalRandom.current().nextInt(0, 90));
            
            Transaction transaction = new Transaction();
            transaction.setAccount(ThreadLocalRandom.current().nextBoolean() ? checkingAccount : creditAccount);
            transaction.setTransactionId("demo-shopping-" + UUID.randomUUID());
            transaction.setDescription(merchant);
            transaction.setAmount(amount);
            transaction.setDate(transactionDate);
            transaction.setCategory(categories.get("Shopping"));
            transaction.setManualEntry(false);
            transactions.add(transaction);
        }
        
        // Add a few savings transfer transactions
        for (int i = 0; i < 3; i++) {
            LocalDate transferDate = LocalDate.now()
                .minusMonths(i)
                .withDayOfMonth(ThreadLocalRandom.current().nextInt(20, 28));
            
            Transaction transferOut = new Transaction();
            transferOut.setAccount(checkingAccount);
            transferOut.setTransactionId("demo-transfer-out-" + UUID.randomUUID());
            transferOut.setDescription("TRANSFER TO SAVINGS");
            transferOut.setAmount(new BigDecimal("-500.00"));
            transferOut.setDate(LocalDateTime.of(transferDate, LocalTime.of(9, 0)));
            transferOut.setCategory(null); // Transfers typically don't have categories
            transferOut.setManualEntry(false);
            transactions.add(transferOut);
            
            Transaction transferIn = new Transaction();
            transferIn.setAccount(savingsAccount);
            transferIn.setTransactionId("demo-transfer-in-" + UUID.randomUUID());
            transferIn.setDescription("TRANSFER FROM CHECKING");
            transferIn.setAmount(new BigDecimal("500.00"));
            transferIn.setDate(LocalDateTime.of(transferDate, LocalTime.of(9, 0)));
            transferIn.setCategory(null); // Transfers typically don't have categories
            transferIn.setManualEntry(false);
            transactions.add(transferIn);
        }
        
        // Save all transactions
        transactionRepository.saveAll(transactions);
        logger.info("Created {} demo transactions", transactions.size());
    }
    
    /**
     * Create demo budgets for the user.
     * 
     * @param user The user to create budgets for
     * @param categories Map of category names to Category objects
     */
    private void createDemoBudgets(User user, Map<String, Category> categories) {
        // Clear any existing budgets
        List<Budget> existingBudgets = budgetRepository.findByUser(user);
        if (!existingBudgets.isEmpty()) {
            logger.info("Removing {} existing budgets", existingBudgets.size());
            budgetRepository.deleteAll(existingBudgets);
        }
        
        List<Budget> budgets = new ArrayList<>();
        
        // Create monthly budgets for common categories
        String[] budgetData = {
            "Monthly Housing|Housing|1500.00|MONTHLY|80",
            "Monthly Food|Food|600.00|MONTHLY|80",
            "Monthly Transportation|Transportation|300.00|MONTHLY|80",
            "Monthly Entertainment|Entertainment|200.00|MONTHLY|90",
            "Monthly Shopping|Shopping|400.00|MONTHLY|80",
            "Monthly Utilities|Utilities|300.00|MONTHLY|90"
        };
        
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        for (String data : budgetData) {
            String[] parts = data.split("\\|");
            String name = parts[0];
            String categoryName = parts[1];
            BigDecimal amount = new BigDecimal(parts[2]);
            String period = parts[3];
            BigDecimal warningThreshold = new BigDecimal(parts[4]);
            
            Budget budget = new Budget();
            budget.setUser(user);
            budget.setName(name);
            budget.setAmount(amount);
            budget.setCategory(categories.get(categoryName));
            budget.setPeriod(period);
            budget.setStartDate(firstDayOfMonth);
            budget.setEndDate(lastDayOfMonth);
            budget.setWarningThreshold(warningThreshold);
            
            budgets.add(budget);
        }
        
        // Save all budgets
        budgetRepository.saveAll(budgets);
        logger.info("Created {} demo budgets", budgets.size());
    }
    
    /**
     * Create demo bills for the user.
     * 
     * @param user The user to create bills for
     */
    private void createDemoBills(User user) {
        // Clear any existing bills
        List<Bill> existingBills = billRepository.findByUser(user);
        if (!existingBills.isEmpty()) {
            logger.info("Removing {} existing bills", existingBills.size());
            billRepository.deleteAll(existingBills);
        }
        
        List<Bill> bills = new ArrayList<>();
        
        // Create common bills
        String[] billData = {
            "Rent|1500.00|1|MONTHLY",
            "Electric|125.00|15|MONTHLY",
            "Water|85.00|20|MONTHLY",
            "Internet|80.00|5|MONTHLY",
            "Phone|95.00|12|MONTHLY",
            "Netflix|14.99|18|MONTHLY",
            "Gym Membership|50.00|23|MONTHLY",
            "Car Insurance|150.00|10|MONTHLY",
            "Health Insurance|200.00|5|MONTHLY"
        };
        
        for (String data : billData) {
            String[] parts = data.split("\\|");
            String name = parts[0];
            BigDecimal amount = new BigDecimal(parts[1]);
            int dueDay = Integer.parseInt(parts[2]);
            String recurringPeriod = parts[3];
            
            Bill bill = new Bill();
            bill.setUser(user);
            bill.setName(name);
            bill.setAmount(amount);
            bill.setDueDay(dueDay);
            bill.setRecurringPeriod(recurringPeriod);
            bill.setAutoPay(ThreadLocalRandom.current().nextBoolean());
            bill.setDescription("Demo " + name + " bill");
            
            bills.add(bill);
        }
        
        // Save all bills
        billRepository.saveAll(bills);
        logger.info("Created {} demo bills", bills.size());
    }
    
    /**
     * Initialize demo data for the demo user if it doesn't already have data.
     * 
     * @return true if successful, false otherwise
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public boolean initializeDemoUserData() {
        try {
            // First try to directly create/ensure demo user via JDBC (most reliable method)
            boolean demoUserCreated = userService.ensureDemoUserExists();
            if (!demoUserCreated) {
                logger.error("Failed to create or verify demo user");
                return false;
            }
            
            // Get the demo user by email (most reliable way)
            User demoUser = null;
            try {
                // Try to get directly from JDBC first
                demoUser = userService.getDemoUserJdbc();
            } catch (Exception e) {
                logger.warn("Error getting demo user via JDBC: {}", e.getMessage());
            }
            
            // Try JPA as fallback if needed
            if (demoUser == null) {
                try {
                    demoUser = userService.getDemoUserByEmail();
                } catch (Exception e) {
                    logger.error("Failed to get demo user by email: {}", e.getMessage());
                    return false;
                }
            }
            
            if (demoUser == null) {
                logger.error("Demo user not found after creation attempts");
                return false;
            }
            
            // Check if demo user already has accounts (indicating data exists)
            List<Account> accounts = null;
            try {
                accounts = accountRepository.findByUser(demoUser);
                if (accounts != null && !accounts.isEmpty()) {
                    logger.info("Demo user already has {} accounts. Skipping data initialization", accounts.size());
                    return true;
                }
            } catch (Exception e) {
                logger.warn("Error checking demo user accounts: {}", e.getMessage());
                // Continue anyway to try creating the data
            }
            
            // Initialize demo data in a new transaction
            logger.info("Creating demo data for demo user with ID: {}", demoUser.getId());
            try {
                return initializeDemoDataWithNewTransaction(demoUser);
            } catch (Exception e) {
                logger.error("Error initializing demo data: {}", e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error in demo user data initialization: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public boolean initializeDemoDataWithNewTransaction(User demoUser) {
        return initializeDemoData(demoUser);
    }
}