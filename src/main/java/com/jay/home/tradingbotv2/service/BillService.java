package com.jay.home.tradingbotv2.service;

import com.jay.home.tradingbotv2.model.Bill;
import com.jay.home.tradingbotv2.model.User;
import com.jay.home.tradingbotv2.repository.BillRepository;
import com.jay.home.tradingbotv2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;

    @Autowired
    public BillService(BillRepository billRepository, UserRepository userRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
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
}