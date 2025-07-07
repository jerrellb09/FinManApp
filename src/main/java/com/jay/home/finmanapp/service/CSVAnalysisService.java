package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.*;
import com.jay.home.finmanapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSVAnalysisService {

    private final TransactionRepository transactionRepository;
    private final BillRepository billRepository;
    private final CategoryRepository categoryRepository;
    private final AIService aiService;

    /**
     * Analyze imported data and provide comprehensive insights
     */
    public Map<String, Object> analyzeImportedData(User user) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Get recent transactions (last 30 days of imports)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Transaction> recentTransactions = transactionRepository.findByAccountUserAndDateAfter(user, thirtyDaysAgo);
            
            // Basic statistics
            analysis.put("totalTransactions", recentTransactions.size());
            analysis.put("dateRange", getDateRange(recentTransactions));
            analysis.put("spendingAnalysis", analyzeSpending(recentTransactions));
            analysis.put("categoryBreakdown", analyzeCategoryBreakdown(recentTransactions));
            analysis.put("monthlyTrends", analyzeMonthlyTrends(recentTransactions));
            
            // AI-generated insights
            String aiInsights = generateAIInsights(user, recentTransactions);
            analysis.put("aiInsights", aiInsights);
            analysis.put("recommendations", generateRecommendations(recentTransactions));
            
            // Bills analysis
            List<Bill> userBills = billRepository.findByUserId(user.getId());
            analysis.put("billsAnalysis", analyzeBills(userBills));
            
        } catch (Exception e) {
            log.error("Error analyzing imported data for user {}", user.getEmail(), e);
            analysis.put("error", "Unable to analyze data: " + e.getMessage());
        }
        
        return analysis;
    }
    
    /**
     * Analyze spending patterns
     */
    public Map<String, Object> analyzeSpendingPatterns(User user) {
        Map<String, Object> patterns = new HashMap<>();
        
        try {
            List<Transaction> transactions = transactionRepository.findByAccountUser(user);
            
            patterns.put("averageTransactionAmount", calculateAverageTransaction(transactions));
            patterns.put("largestExpenses", findLargestExpenses(transactions, 5));
            patterns.put("frequentMerchants", findFrequentMerchants(transactions));
            patterns.put("spendingByDayOfWeek", analyzeSpendingByDayOfWeek(transactions));
            patterns.put("monthlySpendingTrend", calculateMonthlySpending(transactions));
            patterns.put("categories", analyzeCategorySpending(transactions));
            
        } catch (Exception e) {
            log.error("Error analyzing spending patterns for user {}", user.getEmail(), e);
            patterns.put("error", "Unable to analyze spending patterns: " + e.getMessage());
        }
        
        return patterns;
    }
    
    /**
     * Assess overall financial health
     */
    public Map<String, Object> assessFinancialHealth(User user) {
        Map<String, Object> assessment = new HashMap<>();
        
        try {
            List<Transaction> transactions = transactionRepository.findByAccountUser(user);
            List<Bill> bills = billRepository.findByUserId(user.getId());
            
            // Calculate key metrics
            BigDecimal totalIncome = calculateTotalIncome(transactions);
            BigDecimal totalExpenses = calculateTotalExpenses(transactions);
            BigDecimal monthlyBills = calculateMonthlyBills(bills);
            
            assessment.put("totalIncome", totalIncome);
            assessment.put("totalExpenses", totalExpenses);
            assessment.put("monthlyBills", monthlyBills);
            assessment.put("savingsRate", calculateSavingsRate(totalIncome, totalExpenses));
            assessment.put("debtToIncomeRatio", calculateDebtToIncomeRatio(bills, totalIncome));
            assessment.put("financialHealthScore", calculateHealthScore(totalIncome, totalExpenses, monthlyBills));
            assessment.put("recommendations", generateHealthRecommendations(totalIncome, totalExpenses, monthlyBills));
            
        } catch (Exception e) {
            log.error("Error assessing financial health for user {}", user.getEmail(), e);
            assessment.put("error", "Unable to assess financial health: " + e.getMessage());
        }
        
        return assessment;
    }
    
    /**
     * Analyze bill optimization opportunities
     */
    public Map<String, Object> analyzeBillOptimization(User user) {
        Map<String, Object> optimization = new HashMap<>();
        
        try {
            List<Bill> bills = billRepository.findByUserId(user.getId());
            
            optimization.put("totalMonthlyBills", calculateTotalMonthlyBills(bills));
            optimization.put("highestBills", findHighestBills(bills, 5));
            optimization.put("optimizationOpportunities", findOptimizationOpportunities(bills));
            optimization.put("billFrequency", analyzeBillFrequency(bills));
            optimization.put("recommendations", generateBillRecommendations(bills));
            
        } catch (Exception e) {
            log.error("Error analyzing bill optimization for user {}", user.getEmail(), e);
            optimization.put("error", "Unable to analyze bill optimization: " + e.getMessage());
        }
        
        return optimization;
    }
    
    // Helper methods
    
    private Map<String, Object> getDateRange(List<Transaction> transactions) {
        if (transactions.isEmpty()) return Map.of();
        
        LocalDateTime earliest = transactions.stream()
                .map(Transaction::getDate)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        
        LocalDateTime latest = transactions.stream()
                .map(Transaction::getDate)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        
        return Map.of(
                "earliest", earliest.toLocalDate(),
                "latest", latest.toLocalDate()
        );
    }
    
    private Map<String, Object> analyzeSpending(List<Transaction> transactions) {
        BigDecimal totalSpending = transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageTransaction = transactions.isEmpty() ? 
                BigDecimal.ZERO : 
                totalSpending.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
        
        return Map.of(
                "total", totalSpending,
                "average", averageTransaction,
                "transactionCount", transactions.size()
        );
    }
    
    private Map<String, BigDecimal> analyzeCategoryBreakdown(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }
    
    private List<Map<String, Object>> analyzeMonthlyTrends(List<Transaction> transactions) {
        Map<String, BigDecimal> monthlySpending = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDate().getYear() + "-" + String.format("%02d", t.getDate().getMonthValue()),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
        
        return monthlySpending.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("month", entry.getKey());
                    result.put("spending", entry.getValue());
                    return result;
                })
                .collect(Collectors.toList());
    }
    
    private String generateAIInsights(User user, List<Transaction> transactions) {
        try {
            // Use existing AI service to generate insights
            Map<String, Object> aiResponse = aiService.generateFinancialInsights(user);
            return aiResponse.getOrDefault("insights", "No AI insights available.").toString();
        } catch (Exception e) {
            log.error("Error generating AI insights", e);
            return "AI insights temporarily unavailable.";
        }
    }
    
    private List<String> generateRecommendations(List<Transaction> transactions) {
        List<String> recommendations = new ArrayList<>();
        
        // Analyze spending patterns and generate recommendations
        BigDecimal totalSpending = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalSpending.compareTo(BigDecimal.valueOf(1000)) > 0) {
            recommendations.add("Consider setting up a monthly budget to track your spending");
        }
        
        // Check for frequent small transactions
        long smallTransactions = transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.valueOf(10)) < 0)
                .count();
        
        if (smallTransactions > 20) {
            recommendations.add("You have many small transactions - consider consolidating purchases to reduce fees");
        }
        
        return recommendations;
    }
    
    private Map<String, Object> analyzeBills(List<Bill> bills) {
        BigDecimal totalMonthly = bills.stream()
                .filter(Bill::isRecurring)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
                "totalRecurringBills", totalMonthly,
                "billCount", bills.size(),
                "upcomingBills", bills.stream()
                        .filter(b -> !b.isPaid())
                        .count()
        );
    }
    
    private BigDecimal calculateAverageTransaction(List<Transaction> transactions) {
        if (transactions.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
    }
    
    private List<Map<String, Object>> findLargestExpenses(List<Transaction> transactions, int limit) {
        return transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
                .limit(limit)
                .map(t -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("description", t.getDescription());
                    result.put("amount", t.getAmount());
                    result.put("date", t.getDate().toLocalDate());
                    return result;
                })
                .collect(Collectors.toList());
    }
    
    private Map<String, Long> findFrequentMerchants(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getDescription,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    private Map<String, BigDecimal> analyzeSpendingByDayOfWeek(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDate().getDayOfWeek().toString(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }
    
    private Map<String, BigDecimal> calculateMonthlySpending(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDate().getYear() + "-" + String.format("%02d", t.getDate().getMonthValue()),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }
    
    private Map<String, BigDecimal> analyzeCategorySpending(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }
    
    private BigDecimal calculateTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .map(Transaction::getAmount)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateTotalExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateMonthlyBills(List<Bill> bills) {
        return bills.stream()
                .filter(Bill::isRecurring)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        return income.subtract(expenses)
                .divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateDebtToIncomeRatio(List<Bill> bills, BigDecimal income) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        BigDecimal totalDebt = bills.stream()
                .filter(b -> b.getCategory() != null && 
                            (b.getCategory().getName().toLowerCase().contains("loan") ||
                             b.getCategory().getName().toLowerCase().contains("debt")))
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalDebt.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
    
    private int calculateHealthScore(BigDecimal income, BigDecimal expenses, BigDecimal bills) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return 0;
        
        BigDecimal savingsRate = calculateSavingsRate(income, expenses);
        int score = 50; // Base score
        
        // Adjust based on savings rate
        if (savingsRate.compareTo(BigDecimal.valueOf(20)) > 0) {
            score += 30;
        } else if (savingsRate.compareTo(BigDecimal.valueOf(10)) > 0) {
            score += 15;
        } else if (savingsRate.compareTo(BigDecimal.ZERO) < 0) {
            score -= 30;
        }
        
        // Adjust based on bill-to-income ratio
        BigDecimal billRatio = bills.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (billRatio.compareTo(BigDecimal.valueOf(30)) < 0) {
            score += 20;
        } else if (billRatio.compareTo(BigDecimal.valueOf(50)) > 0) {
            score -= 20;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private List<String> generateHealthRecommendations(BigDecimal income, BigDecimal expenses, BigDecimal bills) {
        List<String> recommendations = new ArrayList<>();
        
        BigDecimal savingsRate = calculateSavingsRate(income, expenses);
        
        if (savingsRate.compareTo(BigDecimal.valueOf(10)) < 0) {
            recommendations.add("Try to save at least 10% of your income each month");
        }
        
        if (expenses.compareTo(income) > 0) {
            recommendations.add("Your expenses exceed your income - consider reducing spending or increasing income");
        }
        
        BigDecimal billRatio = income.compareTo(BigDecimal.ZERO) > 0 ? 
                bills.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;
        
        if (billRatio.compareTo(BigDecimal.valueOf(40)) > 0) {
            recommendations.add("Your fixed bills are high relative to income - look for ways to reduce recurring expenses");
        }
        
        return recommendations;
    }
    
    private BigDecimal calculateTotalMonthlyBills(List<Bill> bills) {
        return bills.stream()
                .filter(Bill::isRecurring)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private List<Map<String, Object>> findHighestBills(List<Bill> bills, int limit) {
        return bills.stream()
                .sorted((b1, b2) -> b2.getAmount().compareTo(b1.getAmount()))
                .limit(limit)
                .map(b -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("name", b.getName());
                    result.put("amount", b.getAmount());
                    result.put("dueDay", b.getDueDay());
                    result.put("category", b.getCategory() != null ? b.getCategory().getName() : "Uncategorized");
                    return result;
                })
                .collect(Collectors.toList());
    }
    
    private List<String> findOptimizationOpportunities(List<Bill> bills) {
        List<String> opportunities = new ArrayList<>();
        
        // Find high subscription costs
        BigDecimal subscriptionTotal = bills.stream()
                .filter(b -> b.getCategory() != null && 
                            b.getCategory().getName().toLowerCase().contains("subscription"))
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (subscriptionTotal.compareTo(BigDecimal.valueOf(100)) > 0) {
            opportunities.add("Review your subscriptions - you're spending $" + subscriptionTotal + " monthly on subscriptions");
        }
        
        // Find multiple bills in same category
        Map<String, Long> categoryCount = bills.stream()
                .filter(b -> b.getCategory() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getCategory().getName(),
                        Collectors.counting()
                ));
        
        categoryCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 2)
                .forEach(entry -> opportunities.add(
                        "You have " + entry.getValue() + " bills in " + entry.getKey() + " category - consider consolidating"
                ));
        
        return opportunities;
    }
    
    private Map<String, Long> analyzeBillFrequency(List<Bill> bills) {
        return bills.stream()
                .collect(Collectors.groupingBy(
                        Bill::getRecurringPeriod,
                        Collectors.counting()
                ));
    }
    
    private List<String> generateBillRecommendations(List<Bill> bills) {
        List<String> recommendations = new ArrayList<>();
        
        BigDecimal totalBills = calculateTotalMonthlyBills(bills);
        
        if (totalBills.compareTo(BigDecimal.valueOf(2000)) > 0) {
            recommendations.add("Your monthly bills are quite high - consider negotiating rates or switching providers");
        }
        
        long unpaidBills = bills.stream()
                .filter(b -> !b.isPaid())
                .count();
        
        if (unpaidBills > 5) {
            recommendations.add("You have many unpaid bills - consider setting up automatic payments to avoid late fees");
        }
        
        return recommendations;
    }
}