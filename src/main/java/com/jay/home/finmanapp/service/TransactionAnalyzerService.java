package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.dto.TransactionImportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionAnalyzerService {

    private static final int MIN_OCCURRENCES = 2;
    private static final int MAX_AMOUNT_VARIANCE_PERCENT = 10;
    private static final int MAX_DAY_VARIANCE = 3;
    
    public List<RecurringTransactionGroup> identifyRecurringTransactions(List<TransactionImportDTO> transactions) {
        List<RecurringTransactionGroup> recurringGroups = new ArrayList<>();
        
        // Group transactions by merchant/name
        Map<String, List<TransactionImportDTO>> transactionsByMerchant = transactions.stream()
                .filter(t -> t.getDisplayName() != null && !t.getDisplayName().trim().isEmpty())
                .collect(Collectors.groupingBy(
                    t -> normalizeTransactionName(t.getDisplayName()),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        
        for (Map.Entry<String, List<TransactionImportDTO>> entry : transactionsByMerchant.entrySet()) {
            String merchantName = entry.getKey();
            List<TransactionImportDTO> merchantTransactions = entry.getValue();
            
            if (merchantTransactions.size() < MIN_OCCURRENCES) {
                continue;
            }
            
            // Sort by date
            merchantTransactions.sort(Comparator.comparing(TransactionImportDTO::getDate));
            
            // Analyze for recurring patterns
            RecurringTransactionGroup group = analyzeRecurringPattern(merchantName, merchantTransactions);
            if (group != null) {
                recurringGroups.add(group);
            }
        }
        
        log.info("Identified {} recurring transaction groups", recurringGroups.size());
        return recurringGroups;
    }
    
    private RecurringTransactionGroup analyzeRecurringPattern(String merchantName, List<TransactionImportDTO> transactions) {
        if (transactions.size() < MIN_OCCURRENCES) {
            return null;
        }
        
        // Calculate periods between transactions
        List<Integer> daysBetween = new ArrayList<>();
        for (int i = 1; i < transactions.size(); i++) {
            LocalDate prev = transactions.get(i - 1).getDate();
            LocalDate curr = transactions.get(i).getDate();
            if (prev != null && curr != null) {
                daysBetween.add(Period.between(prev, curr).getDays());
            }
        }
        
        if (daysBetween.isEmpty()) {
            return null;
        }
        
        // Check for consistent patterns
        RecurringPeriod period = identifyPeriod(daysBetween);
        if (period == null) {
            return null;
        }
        
        // Check amount consistency
        BigDecimal avgAmount = calculateAverageAmount(transactions);
        if (!isAmountConsistent(transactions, avgAmount)) {
            return null;
        }
        
        // Check if it's a bill-like transaction (positive amount = expense)
        boolean isBill = transactions.stream()
                .anyMatch(t -> t.isDebit() && isLikelyBill(t.getCategory()));
        
        return RecurringTransactionGroup.builder()
                .merchantName(merchantName)
                .transactions(transactions)
                .period(period)
                .averageAmount(avgAmount)
                .isBill(isBill)
                .category(getMostCommonCategory(transactions))
                .build();
    }
    
    private RecurringPeriod identifyPeriod(List<Integer> daysBetween) {
        // Calculate average days between transactions
        double avgDays = daysBetween.stream().mapToInt(Integer::intValue).average().orElse(0);
        
        // Check for monthly pattern (28-31 days)
        if (avgDays >= 28 && avgDays <= 31) {
            return RecurringPeriod.MONTHLY;
        }
        
        // Check for weekly pattern (7 days ± 1)
        if (avgDays >= 6 && avgDays <= 8) {
            return RecurringPeriod.WEEKLY;
        }
        
        // Check for bi-weekly pattern (14 days ± 2)
        if (avgDays >= 12 && avgDays <= 16) {
            return RecurringPeriod.BIWEEKLY;
        }
        
        // Check for quarterly pattern (90 days ± 7)
        if (avgDays >= 83 && avgDays <= 97) {
            return RecurringPeriod.QUARTERLY;
        }
        
        // Check for annual pattern (365 days ± 30)
        if (avgDays >= 335 && avgDays <= 395) {
            return RecurringPeriod.ANNUALLY;
        }
        
        return null;
    }
    
    private BigDecimal calculateAverageAmount(List<TransactionImportDTO> transactions) {
        return transactions.stream()
                .filter(t -> t.getAmount() != null)
                .map(TransactionImportDTO::getAbsoluteAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(transactions.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    private boolean isAmountConsistent(List<TransactionImportDTO> transactions, BigDecimal avgAmount) {
        return transactions.stream()
                .filter(t -> t.getAmount() != null)
                .allMatch(t -> {
                    BigDecimal amount = t.getAbsoluteAmount();
                    BigDecimal variance = amount.subtract(avgAmount).abs();
                    BigDecimal maxVariance = avgAmount.multiply(BigDecimal.valueOf(MAX_AMOUNT_VARIANCE_PERCENT / 100.0));
                    return variance.compareTo(maxVariance) <= 0;
                });
    }
    
    private boolean isLikelyBill(String category) {
        if (category == null) return false;
        
        String lowerCategory = category.toLowerCase();
        return lowerCategory.contains("bill") || 
               lowerCategory.contains("utilities") || 
               lowerCategory.contains("insurance") || 
               lowerCategory.contains("subscription") ||
               lowerCategory.contains("rent") ||
               lowerCategory.contains("mortgage") ||
               lowerCategory.contains("loan");
    }
    
    private String getMostCommonCategory(List<TransactionImportDTO> transactions) {
        return transactions.stream()
                .map(TransactionImportDTO::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Uncategorized");
    }
    
    private String normalizeTransactionName(String name) {
        if (name == null) return "";
        
        // Remove common transaction ID patterns and normalize
        return name.toUpperCase()
                .replaceAll("\\s+", " ")
                .replaceAll("\\d{4,}", "") // Remove long numbers
                .replaceAll("\\*\\w+", "") // Remove asterisk patterns
                .replaceAll("WEB ID:.*", "") // Remove web IDs
                .replaceAll("PPD ID:.*", "") // Remove PPD IDs
                .trim();
    }
    
    public static class RecurringTransactionGroup {
        private String merchantName;
        private List<TransactionImportDTO> transactions;
        private RecurringPeriod period;
        private BigDecimal averageAmount;
        private boolean isBill;
        private String category;
        
        public static RecurringTransactionGroupBuilder builder() {
            return new RecurringTransactionGroupBuilder();
        }
        
        // Getters
        public String getMerchantName() { return merchantName; }
        public List<TransactionImportDTO> getTransactions() { return transactions; }
        public RecurringPeriod getPeriod() { return period; }
        public BigDecimal getAverageAmount() { return averageAmount; }
        public boolean isBill() { return isBill; }
        public String getCategory() { return category; }
        
        public static class RecurringTransactionGroupBuilder {
            private String merchantName;
            private List<TransactionImportDTO> transactions;
            private RecurringPeriod period;
            private BigDecimal averageAmount;
            private boolean isBill;
            private String category;
            
            public RecurringTransactionGroupBuilder merchantName(String merchantName) {
                this.merchantName = merchantName;
                return this;
            }
            
            public RecurringTransactionGroupBuilder transactions(List<TransactionImportDTO> transactions) {
                this.transactions = transactions;
                return this;
            }
            
            public RecurringTransactionGroupBuilder period(RecurringPeriod period) {
                this.period = period;
                return this;
            }
            
            public RecurringTransactionGroupBuilder averageAmount(BigDecimal averageAmount) {
                this.averageAmount = averageAmount;
                return this;
            }
            
            public RecurringTransactionGroupBuilder isBill(boolean isBill) {
                this.isBill = isBill;
                return this;
            }
            
            public RecurringTransactionGroupBuilder category(String category) {
                this.category = category;
                return this;
            }
            
            public RecurringTransactionGroup build() {
                RecurringTransactionGroup group = new RecurringTransactionGroup();
                group.merchantName = this.merchantName;
                group.transactions = this.transactions;
                group.period = this.period;
                group.averageAmount = this.averageAmount;
                group.isBill = this.isBill;
                group.category = this.category;
                return group;
            }
        }
    }
    
    public enum RecurringPeriod {
        WEEKLY,
        BIWEEKLY,
        MONTHLY,
        QUARTERLY,
        ANNUALLY
    }
}