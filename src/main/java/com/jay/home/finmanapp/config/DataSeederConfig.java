package com.jay.home.finmanapp.config;

import com.jay.home.finmanapp.model.*;
import com.jay.home.finmanapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
// Enable for all profiles for development
// @Profile("!h2") // Only run this when NOT using h2 profile
public class DataSeederConfig {
    
    @Autowired
    private Environment env;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner seedData() {
        return args -> {
            try {
                // Check if we need to create the demo user (always ensure it exists)
                if (userRepository.findByIsDemo(true).isEmpty()) {
                    System.out.println("Creating demo user account...");
                    User demoUser = new User();
                    demoUser.setEmail("demo@finmanapp.com");
                    demoUser.setPassword(passwordEncoder.encode("demo123")); // Password won't be used directly
                    demoUser.setFirstName("Demo");
                    demoUser.setLastName("User");
                    demoUser.setMonthlyIncome(new BigDecimal("5000.00"));
                    demoUser.setPaydayDay(15);
                    demoUser.setIsDemo(true);
                    userRepository.save(demoUser);
                    System.out.println("Demo user created successfully.");
                } else {
                    System.out.println("Demo user already exists.");
                }
                
                // Only seed the rest if the database is empty
                if (userRepository.count() <= 1) { // 1 = only the demo user
                    System.out.println("Seeding database with initial data...");
                    
                    // Seed categories
                    seedCategories();
                    
                    // Seed regular users
                    User user1 = seedUser("test@example.com", "password", "Test", "User", new BigDecimal("5000.00"), 15);
                    User user2 = seedUser("jane@example.com", "password", "Jane", "Smith", new BigDecimal("6500.00"), 1);
                    
                    // Get the demo user for seeding demo data
                    User demoUser = userRepository.findByIsDemo(true).get();
                
                    // Create demo accounts
                    Account demoChecking = seedAccount(demoUser, "Demo Checking", "CHECKING", new BigDecimal("3200.00"), "acc_demo_checking", "access_token_demo", "ins_demo", "Demo Bank");
                    Account demoSavings = seedAccount(demoUser, "Demo Savings", "SAVINGS", new BigDecimal("12500.00"), "acc_demo_savings", "access_token_demo", "ins_demo", "Demo Bank");
                    Account demoCredit = seedAccount(demoUser, "Demo Credit Card", "CREDIT", new BigDecimal("-750.00"), "acc_demo_credit", "access_token_demo", "ins_demo", "Demo Credit Union");
                    Account demoInvestment = seedAccount(demoUser, "Demo Investment", "INVESTMENT", new BigDecimal("25000.00"), "acc_demo_invest", "access_token_demo", "ins_demo", "Demo Investments");
                
                    // Seed regular user accounts
                    Account checking1 = seedAccount(user1, "Checking Account", "CHECKING", new BigDecimal("2500.00"), "acc_123456", "access_token_checking", "ins_12345", "Big Bank");
                    Account savings1 = seedAccount(user1, "Savings Account", "SAVINGS", new BigDecimal("10000.00"), "acc_789012", "access_token_savings", "ins_12345", "Big Bank");
                    Account creditCard1 = seedAccount(user1, "Credit Card", "CREDIT", new BigDecimal("-1500.00"), "acc_345678", "access_token_cc", "ins_67890", "Credit Bank");
                    
                    Account checking2 = seedAccount(user2, "Primary Checking", "CHECKING", new BigDecimal("3500.00"), "acc_jane123", "access_token_jane_checking", "ins_12345", "Big Bank");
                    Account savings2 = seedAccount(user2, "High-Yield Savings", "SAVINGS", new BigDecimal("15000.00"), "acc_jane456", "access_token_jane_savings", "ins_12345", "Big Bank");
                
                // Get categories
                Category housing = categoryRepository.findByName("Housing").orElse(null);
                Category transportation = categoryRepository.findByName("Transportation").orElse(null);
                Category food = categoryRepository.findByName("Food").orElse(null);
                Category entertainment = categoryRepository.findByName("Entertainment").orElse(null);
                Category shopping = categoryRepository.findByName("Shopping").orElse(null);
                Category utilities = categoryRepository.findByName("Utilities").orElse(null);
                Category subscriptions = categoryRepository.findByName("Subscriptions").orElse(null);
                Category income = categoryRepository.findByName("Income").orElse(null);
                
                // Seed budgets
                Budget rent1 = seedBudget(user1, "Monthly Rent", new BigDecimal("1200.00"), housing, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("90.00"));
                Budget groceries1 = seedBudget(user1, "Groceries", new BigDecimal("500.00"), food, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("80.00"));
                Budget entertainment1 = seedBudget(user1, "Entertainment", new BigDecimal("200.00"), entertainment, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("85.00"));
                Budget eatingOut1 = seedBudget(user1, "Eating Out", new BigDecimal("300.00"), food, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("75.00"));
                
                Budget housing2 = seedBudget(user2, "Housing", new BigDecimal("1500.00"), housing, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("90.00"));
                Budget food2 = seedBudget(user2, "Food Budget", new BigDecimal("600.00"), food, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("80.00"));
                Budget transportation2 = seedBudget(user2, "Transportation", new BigDecimal("400.00"), transportation, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("85.00"));
                
                // Seed demo user budgets
                Budget demoRent = seedBudget(demoUser, "Rent/Mortgage", new BigDecimal("1400.00"), housing, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("90.00"));
                Budget demoGroceries = seedBudget(demoUser, "Groceries", new BigDecimal("550.00"), food, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("80.00"));
                Budget demoEntertainment = seedBudget(demoUser, "Entertainment", new BigDecimal("250.00"), entertainment, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("85.00"));
                Budget demoEatingOut = seedBudget(demoUser, "Dining Out", new BigDecimal("300.00"), food, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("75.00"));
                Budget demoTransport = seedBudget(demoUser, "Transportation", new BigDecimal("350.00"), transportation, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("70.00"));
                Budget demoShopping = seedBudget(demoUser, "Shopping", new BigDecimal("200.00"), shopping, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("60.00"));
                Budget demoUtilities = seedBudget(demoUser, "Utilities", new BigDecimal("280.00"), utilities, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("85.00"));
                Budget demoSubscriptions = seedBudget(demoUser, "Subscriptions", new BigDecimal("80.00"), subscriptions, "MONTHLY", LocalDate.now(), LocalDate.now().plusYears(1), new BigDecimal("90.00"));
                
                // Seed transactions for first user
                seedTransaction(checking1, "tx_12345", "Whole Foods Market", new BigDecimal("-85.27"), LocalDateTime.now().minusDays(5), food, false);
                seedTransaction(checking1, "tx_23456", "Amazon.com", new BigDecimal("-29.99"), LocalDateTime.now().minusDays(3), shopping, false);
                seedTransaction(checking1, "tx_34567", "Netflix", new BigDecimal("-13.99"), LocalDateTime.now().minusDays(7), subscriptions, false);
                seedTransaction(checking1, "tx_45678", "Shell Gas Station", new BigDecimal("-45.00"), LocalDateTime.now().minusDays(2), transportation, false);
                seedTransaction(checking1, "tx_56789", "Rent Payment", new BigDecimal("-1200.00"), LocalDateTime.now().minusDays(15), housing, false);
                seedTransaction(checking1, "tx_67890", "Salary Deposit", new BigDecimal("2500.00"), LocalDateTime.now().minusDays(15), income, false);
                seedTransaction(savings1, "tx_78901", "Interest Payment", new BigDecimal("5.25"), LocalDateTime.now().minusDays(1), income, false);
                seedTransaction(creditCard1, "tx_89012", "Restaurant Payment", new BigDecimal("-62.47"), LocalDateTime.now().minusDays(4), food, false);
                seedTransaction(creditCard1, "tx_90123", "Online Shopping", new BigDecimal("-59.99"), LocalDateTime.now().minusDays(6), shopping, false);
                
                // Seed transactions for second user
                seedTransaction(checking2, "tx_jane1", "Trader Joe's", new BigDecimal("-92.45"), LocalDateTime.now().minusDays(2), food, false);
                seedTransaction(checking2, "tx_jane2", "Monthly Transit Pass", new BigDecimal("-120.00"), LocalDateTime.now().minusDays(10), transportation, false);
                seedTransaction(checking2, "tx_jane3", "Paycheck", new BigDecimal("3250.00"), LocalDateTime.now().minusDays(15), income, false);
                seedTransaction(savings2, "tx_jane4", "Interest Earned", new BigDecimal("12.50"), LocalDateTime.now().minusDays(1), income, false);
                
                // Seed demo user transactions
                // Recent transactions (last 7 days)
                seedTransaction(demoChecking, "tx_demo_1", "Whole Foods", new BigDecimal("-112.45"), LocalDateTime.now().minusDays(2), food, false);
                seedTransaction(demoChecking, "tx_demo_2", "Amazon.com", new BigDecimal("-47.99"), LocalDateTime.now().minusDays(3), shopping, false);
                seedTransaction(demoChecking, "tx_demo_3", "Shell Gas", new BigDecimal("-42.50"), LocalDateTime.now().minusDays(1), transportation, false);
                seedTransaction(demoChecking, "tx_demo_4", "Netflix", new BigDecimal("-14.99"), LocalDateTime.now().minusDays(5), subscriptions, false);
                seedTransaction(demoCredit, "tx_demo_5", "Starbucks", new BigDecimal("-6.75"), LocalDateTime.now().minusDays(1), food, false);
                seedTransaction(demoCredit, "tx_demo_6", "Uber", new BigDecimal("-18.45"), LocalDateTime.now().minusDays(2), transportation, false);
                seedTransaction(demoCredit, "tx_demo_7", "Apple Music", new BigDecimal("-9.99"), LocalDateTime.now().minusDays(4), subscriptions, false);
                
                // Older transactions (8-30 days ago)
                seedTransaction(demoChecking, "tx_demo_8", "Phone Bill", new BigDecimal("-85.00"), LocalDateTime.now().minusDays(15), utilities, false);
                seedTransaction(demoChecking, "tx_demo_9", "Internet", new BigDecimal("-75.00"), LocalDateTime.now().minusDays(15), utilities, false);
                seedTransaction(demoChecking, "tx_demo_10", "Rent Payment", new BigDecimal("-1400.00"), LocalDateTime.now().minusDays(30), housing, false);
                seedTransaction(demoChecking, "tx_demo_11", "Salary Deposit", new BigDecimal("5000.00"), LocalDateTime.now().minusDays(15), income, false);
                seedTransaction(demoChecking, "tx_demo_12", "Gym Membership", new BigDecimal("-45.00"), LocalDateTime.now().minusDays(10), subscriptions, false);
                seedTransaction(demoSavings, "tx_demo_13", "Transfer from Checking", new BigDecimal("500.00"), LocalDateTime.now().minusDays(15), income, false);
                seedTransaction(demoSavings, "tx_demo_14", "Interest Payment", new BigDecimal("10.25"), LocalDateTime.now().minusDays(1), income, false);
                
                // More transactions to show patterns (31-90 days)
                seedTransaction(demoChecking, "tx_demo_15", "Whole Foods", new BigDecimal("-105.32"), LocalDateTime.now().minusDays(32), food, false);
                seedTransaction(demoChecking, "tx_demo_16", "Shell Gas", new BigDecimal("-44.25"), LocalDateTime.now().minusDays(31), transportation, false);
                seedTransaction(demoChecking, "tx_demo_17", "Rent Payment", new BigDecimal("-1400.00"), LocalDateTime.now().minusDays(60), housing, false);
                seedTransaction(demoChecking, "tx_demo_18", "Salary Deposit", new BigDecimal("5000.00"), LocalDateTime.now().minusDays(45), income, false);
                seedTransaction(demoChecking, "tx_demo_19", "Electric Bill", new BigDecimal("-95.00"), LocalDateTime.now().minusDays(45), utilities, false);
                seedTransaction(demoChecking, "tx_demo_20", "Car Insurance", new BigDecimal("-120.00"), LocalDateTime.now().minusDays(40), transportation, false);
                seedTransaction(demoCredit, "tx_demo_21", "Amazon.com", new BigDecimal("-65.49"), LocalDateTime.now().minusDays(35), shopping, false);
                seedTransaction(demoCredit, "tx_demo_22", "Restaurant", new BigDecimal("-78.25"), LocalDateTime.now().minusDays(38), food, false);
                seedTransaction(demoSavings, "tx_demo_23", "Transfer from Checking", new BigDecimal("500.00"), LocalDateTime.now().minusDays(45), income, false);
                
                // Seed bills
                seedBill(user1, "Rent", new BigDecimal("1200.00"), 1, true, true, housing);
                seedBill(user1, "Electricity", new BigDecimal("75.00"), 15, false, true, utilities);
                seedBill(user1, "Internet", new BigDecimal("60.00"), 20, false, true, utilities);
                seedBill(user1, "Cell Phone", new BigDecimal("85.00"), 5, true, true, utilities);
                seedBill(user1, "Netflix", new BigDecimal("13.99"), 7, false, true, subscriptions);
                seedBill(user1, "Gym Membership", new BigDecimal("45.00"), 10, false, true, subscriptions);
                
                seedBill(user2, "Mortgage", new BigDecimal("1500.00"), 5, true, true, housing);
                seedBill(user2, "Car Payment", new BigDecimal("350.00"), 12, false, true, transportation);
                seedBill(user2, "Water & Sewage", new BigDecimal("45.00"), 18, false, true, utilities);
                seedBill(user2, "Internet & Cable", new BigDecimal("120.00"), 22, false, true, utilities);
                seedBill(user2, "Streaming Services", new BigDecimal("35.99"), 25, false, true, subscriptions);
                
                // Seed demo user bills
                seedBill(demoUser, "Rent/Mortgage", new BigDecimal("1400.00"), 1, true, true, housing);
                seedBill(demoUser, "Electricity", new BigDecimal("95.00"), 15, false, true, utilities);
                seedBill(demoUser, "Water", new BigDecimal("45.00"), 18, false, true, utilities);
                seedBill(demoUser, "Internet", new BigDecimal("75.00"), 20, false, true, utilities);
                seedBill(demoUser, "Phone", new BigDecimal("85.00"), 5, true, true, utilities);
                seedBill(demoUser, "Netflix", new BigDecimal("14.99"), 7, false, true, subscriptions);
                seedBill(demoUser, "Spotify", new BigDecimal("9.99"), 10, false, true, subscriptions);
                seedBill(demoUser, "Gym Membership", new BigDecimal("45.00"), 12, false, true, subscriptions);
                seedBill(demoUser, "Car Insurance", new BigDecimal("120.00"), 25, false, true, transportation);
                
                System.out.println("Database seeding completed successfully.");
                } else {
                    System.out.println("Database already has data. Skipping seeding.");
                }
            } catch (Exception e) {
                System.err.println("Error seeding database: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    private void seedCategories() {
        List<Category> categories = Arrays.asList(
            createCategory("Housing", "Rent, mortgage, property taxes, utilities, home insurance, repairs", "house-fill"),
            createCategory("Transportation", "Car payments, gas, public transit, vehicle maintenance, insurance", "car-front-fill"),
            createCategory("Food", "Groceries, restaurants, meal delivery", "basket-fill"),
            createCategory("Entertainment", "Streaming services, events, activities", "film"),
            createCategory("Healthcare", "Insurance, medications, doctor visits", "heart-pulse-fill"),
            createCategory("Personal", "Clothing, personal care, haircuts", "person-fill"),
            createCategory("Education", "Tuition, books, courses, student loans", "book-fill"),
            createCategory("Savings", "Emergency fund, investments, retirement", "piggy-bank-fill"),
            createCategory("Debt", "Credit card payments, loans", "credit-card-fill"),
            createCategory("Travel", "Flights, accommodations, vacation expenses", "airplane-fill"),
            createCategory("Shopping", "Retail purchases, household items", "bag-fill"),
            createCategory("Utilities", "Electricity, water, gas, internet, phone", "phone-fill"),
            createCategory("Gifts", "Birthday, holiday, special occasions", "gift-fill"),
            createCategory("Taxes", "Income tax, property tax, other taxes", "cash-stack"),
            createCategory("Insurance", "Life, health, home, auto insurance", "shield-fill"),
            createCategory("Subscriptions", "Recurring subscriptions, memberships", "calendar-check-fill"),
            createCategory("Other", "Miscellaneous expenses that don't fit elsewhere", "three-dots"),
            createCategory("Income", "Salary, side hustle, investments, etc.", "cash")
        );
        
        categoryRepository.saveAll(categories);
    }
    
    private Category createCategory(String name, String description, String iconUrl) {
        Optional<Category> existingCategory = categoryRepository.findByName(name);
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }
        
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIconUrl(iconUrl);
        return category;
    }
    
    private User seedUser(String email, String password, String firstName, String lastName, BigDecimal monthlyIncome, Integer paydayDay) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setMonthlyIncome(monthlyIncome);
        user.setPaydayDay(paydayDay);
        
        return userRepository.save(user);
    }
    
    private Account seedAccount(User user, String name, String type, BigDecimal balance, String accountId, 
                                String accessToken, String institutionId, String institutionName) {
        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        account.setType(type);
        account.setBalance(balance);
        account.setAccountId(accountId);
        account.setAccessToken(accessToken);
        account.setInstitutionId(institutionId);
        account.setInstitutionName(institutionName);
        account.setLastSynced(LocalDateTime.now());
        
        return accountRepository.save(account);
    }
    
    private Budget seedBudget(User user, String name, BigDecimal amount, Category category, String period, 
                             LocalDate startDate, LocalDate endDate, BigDecimal warningThreshold) {
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setName(name);
        budget.setAmount(amount);
        budget.setCategory(category);
        budget.setPeriod(period);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setWarningThreshold(warningThreshold);
        
        return budgetRepository.save(budget);
    }
    
    private Transaction seedTransaction(Account account, String transactionId, String description, BigDecimal amount,
                                       LocalDateTime date, Category category, boolean isManualEntry) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionId(transactionId);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setCategory(category);
        transaction.setManualEntry(isManualEntry);
        
        return transactionRepository.save(transaction);
    }
    
    private Bill seedBill(User user, String name, BigDecimal amount, int dueDay, boolean isPaid, 
                          boolean isRecurring, Category category) {
        Bill bill = new Bill();
        bill.setUser(user);
        bill.setName(name);
        bill.setAmount(amount);
        bill.setDueDay(dueDay);
        bill.setPaid(isPaid);
        bill.setRecurring(isRecurring);
        bill.setCategory(category);
        
        return billRepository.save(bill);
    }
}