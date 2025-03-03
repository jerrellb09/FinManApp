package com.jay.home.tradingbotv2.service;

import com.jay.home.tradingbotv2.model.Account;
import com.jay.home.tradingbotv2.model.Bill;
import com.jay.home.tradingbotv2.model.Category;
import com.jay.home.tradingbotv2.model.User;
import com.jay.home.tradingbotv2.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsightService {
    
    private final BillRepository billRepository;
    private final UserService userService;
    
    @Autowired
    public InsightService(BillRepository billRepository, UserService userService) {
        this.billRepository = billRepository;
        this.userService = userService;
    }

    public List<Map<String, Object>> getSpendingByCategory(List<Account> accounts, LocalDate startDate, LocalDate endDate) {
        // Implement the logic to get spending by category
        return null;
    }

    public List<Map<String, Object>> getSpendingTrend(List<Account> accounts, LocalDate startDate, LocalDate endDate, String period) {
        // Implement the logic to get spending trend
        return null;
    }

    public List<Map<String, Object>> getCategoryTrend(List<Account> accounts, Category category, LocalDate startDate, LocalDate endDate, String period) {
        // Implement the logic to get category trend
        return null;
    }

    public List<Map<String, Object>> getBudgetPerformance(User user, LocalDate startDate, LocalDate endDate) {
        // Implement the logic to get budget performance
        return null;
    }

    public Map<String, Object> getMonthlySummary(User user, int year, int month) {
        // Implement the logic to get monthly summary
        return null;
    }

    public List<Map<String, Object>> generateBudgetSuggestions(User user) {
        // Implement the logic to generate budget suggestions
        return null;
    }

    public List<Map<String, Object>> getTopMerchants(List<Account> accounts, LocalDate startDate, LocalDate endDate, int limit) {
        // Implement the logic to get top merchants
        return null;
    }
    
    public Map<String, Object> getBillsVsIncomeInsight(Long userId) {
        User user = userService.getUserById(userId);
        Map<String, Object> result = new HashMap<>();
        
        // Get monthly income
        BigDecimal monthlyIncome = user.getMonthlyIncome();
        if (monthlyIncome == null) {
            monthlyIncome = BigDecimal.ZERO;
        }
        
        // Get total bill amount
        Double totalBillsAmount = billRepository.getTotalUnpaidBillsAmount(userId);
        BigDecimal billsTotal = totalBillsAmount != null 
            ? BigDecimal.valueOf(totalBillsAmount) 
            : BigDecimal.ZERO;
        
        // Calculate remaining income after bills
        BigDecimal remainingIncome = monthlyIncome.subtract(billsTotal);
        
        // Calculate percentage of income spent on bills
        BigDecimal billPercentage = monthlyIncome.compareTo(BigDecimal.ZERO) > 0
            ? billsTotal.multiply(BigDecimal.valueOf(100)).divide(monthlyIncome, 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
            
        // Get upcoming bills (due in next 7 days)
        int currentDay = LocalDate.now().getDayOfMonth();
        int nextWeekDay = currentDay + 7;
        List<Bill> upcomingBills = billRepository.findDueBills(userId, nextWeekDay);
            
        result.put("monthlyIncome", monthlyIncome);
        result.put("totalBills", billsTotal);
        result.put("remainingIncome", remainingIncome);
        result.put("billPercentage", billPercentage);
        result.put("upcomingBills", upcomingBills);
        result.put("paydayDay", user.getPaydayDay());
        
        return result;
    }
}
