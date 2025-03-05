package com.jay.home.tradingbotv2.service;

import com.jay.home.tradingbotv2.model.Bill;
import com.jay.home.tradingbotv2.model.Category;
import com.jay.home.tradingbotv2.model.User;
import com.jay.home.tradingbotv2.repository.BillRepository;
import com.jay.home.tradingbotv2.repository.CategoryRepository;
import com.jay.home.tradingbotv2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public BillService(BillRepository billRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Bill createBill(Bill bill, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        bill.setUser(user);
        return billRepository.save(bill);
    }

    public List<Bill> getUserBills(Long userId) {
        return billRepository.findByUserId(userId);
    }

    public List<Bill> getDueBills(Long userId) {
        int currentDay = LocalDate.now().getDayOfMonth();
        return billRepository.findDueBills(userId, currentDay);
    }

    public Bill updateBill(Long billId, Bill billDetails) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        
        bill.setName(billDetails.getName());
        bill.setAmount(billDetails.getAmount());
        bill.setDueDay(billDetails.getDueDay());
        bill.setPaid(billDetails.isPaid());
        bill.setRecurring(billDetails.isRecurring());
        bill.setCategory(billDetails.getCategory());
        
        return billRepository.save(bill);
    }
    
    public void deleteBill(Long billId) {
        billRepository.deleteById(billId);
    }
    
    public void markBillAsPaid(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        bill.setPaid(true);
        billRepository.save(bill);
    }
    
    public BigDecimal getRemainingIncome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal monthlyIncome = user.getMonthlyIncome();
        if (monthlyIncome == null) {
            return BigDecimal.ZERO;
        }
        
        Double unpaidBillsAmount = billRepository.getTotalUnpaidBillsAmount(userId);
        if (unpaidBillsAmount == null) {
            return monthlyIncome;
        }
        
        return monthlyIncome.subtract(BigDecimal.valueOf(unpaidBillsAmount));
    }
    
    public void resetMonthlyBills(Long userId) {
        List<Bill> bills = billRepository.findByUserIdAndIsPaid(userId, true);
        for (Bill bill : bills) {
            if (bill.isRecurring()) {
                bill.setPaid(false);
                billRepository.save(bill);
            }
        }
    }
    
    public Map<String, List<Bill>> getBillsByCategory(Long userId) {
        List<Bill> userBills = billRepository.findByUserId(userId);
        Map<String, List<Bill>> billsByCategory = new HashMap<>();
        
        // Initialize for each category
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            billsByCategory.put(category.getName(), new ArrayList<>());
        }
        
        // Add "Uncategorized" category
        billsByCategory.put("Uncategorized", new ArrayList<>());
        
        // Group bills by category
        for (Bill bill : userBills) {
            if (bill.getCategory() != null) {
                String categoryName = bill.getCategory().getName();
                billsByCategory.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(bill);
            } else {
                billsByCategory.get("Uncategorized").add(bill);
            }
        }
        
        return billsByCategory;
    }
    
    public List<Bill> getUpcomingBills(Long userId, int days) {
        int currentDay = LocalDate.now().getDayOfMonth();
        int lastDayToCheck = currentDay + days;
        int daysInMonth = LocalDate.now().lengthOfMonth();
        
        List<Bill> userBills = billRepository.findByUserId(userId);
        return userBills.stream()
                .filter(bill -> !bill.isPaid())
                .filter(bill -> {
                    int dueDay = bill.getDueDay();
                    return (dueDay >= currentDay && dueDay <= lastDayToCheck) ||
                           (lastDayToCheck > daysInMonth && dueDay <= (lastDayToCheck - daysInMonth));
                })
                .collect(Collectors.toList());
    }
    
    public BigDecimal getMonthlyBillsTotal(Long userId) {
        List<Bill> userBills = billRepository.findByUserId(userId);
        BigDecimal total = userBills.stream()
                .filter(Bill::isRecurring)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }
}