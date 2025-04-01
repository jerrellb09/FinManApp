package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.Bill;
import com.jay.home.finmanapp.model.Category;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.repository.BillRepository;
import com.jay.home.finmanapp.repository.CategoryRepository;
import com.jay.home.finmanapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for managing bill-related business logic.
 *
 * This service provides methods for creating, retrieving, updating, and deleting bills,
 * as well as specialized operations such as marking bills as paid/unpaid, calculating
 * remaining income, and resetting monthly bills. It acts as an intermediary between
 * the controllers and the data access layer, implementing business rules and validations.
 *
 * The service maintains relationships between bills and associated entities like users
 * and categories, ensuring data integrity and proper access control.
 */
@Service
public class BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Constructs a new BillService with the required repositories.
     *
     * @param billRepository Repository for bill data access
     * @param userRepository Repository for user data access
     * @param categoryRepository Repository for category data access
     */
    @Autowired
    public BillService(BillRepository billRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a new bill for a specific user.
     * 
     * This method associates the bill with the specified user and persists it to the database.
     * If the user doesn't exist, a RuntimeException is thrown.
     *
     * @param bill The bill entity to create
     * @param userId The ID of the user who owns this bill
     * @return The created bill with its generated ID
     * @throws RuntimeException if the specified user doesn't exist
     */
    public Bill createBill(Bill bill, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        bill.setUser(user);
        return billRepository.save(bill);
    }

    /**
     * Retrieves all bills for a specific user.
     * 
     * This method returns all bills associated with the specified user ID,
     * regardless of their payment status or due date.
     *
     * @param userId The ID of the user whose bills are being retrieved
     * @return A list of bills associated with the user
     */
    public List<Bill> getUserBills(Long userId) {
        return billRepository.findByUserId(userId);
    }

    /**
     * Retrieves bills that are due today for a specific user.
     * 
     * This method returns bills whose due day matches the current day of the month
     * and that belong to the specified user.
     *
     * @param userId The ID of the user whose due bills are being retrieved
     * @return A list of bills due today for the specified user
     */
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
    
    public void markBillAsUnpaid(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        bill.setPaid(false);
        billRepository.save(bill);
    }
    
    public Bill getBillById(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
    }
    
    public List<Bill> getUserBillsByCategory(Long userId, Long categoryId) {
        return billRepository.findByUserIdAndCategoryId(userId, categoryId);
    }
    
    /**
     * Calculates the remaining income for a user after accounting for unpaid bills.
     * 
     * This method subtracts the total amount of unpaid bills from the user's monthly income
     * to determine how much discretionary income remains. If the user's monthly income
     * is not set, it returns zero. If there are no unpaid bills, it returns the full
     * monthly income.
     *
     * @param userId The ID of the user for whom to calculate remaining income
     * @return The remaining income amount after accounting for unpaid bills
     * @throws RuntimeException if the specified user doesn't exist
     */
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
    
    /**
     * Resets the payment status of all recurring bills for a user.
     * 
     * This method is typically called at the beginning of a new billing cycle (like a new month).
     * It finds all bills that are marked as paid and recurring, and resets their payment status
     * to unpaid, indicating that they need to be paid in the new cycle.
     *
     * @param userId The ID of the user whose bills should be reset
     */
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
    
    /**
     * Retrieves unpaid bills that are due within a specified number of days.
     * 
     * This method calculates which bills are coming due within the specified time window,
     * considering only unpaid bills. It handles month boundaries correctly, so if the
     * specified time window crosses into the next month, bills due in the early days of
     * the next month are also included.
     *
     * For example, if today is the 25th day of a 30-day month, and the days parameter is 10,
     * the method will include bills due on days 25-30 of the current month and days 1-5 of
     * the next month.
     *
     * @param userId The ID of the user whose upcoming bills are being retrieved
     * @param days The number of days to look ahead for upcoming bills
     * @return A list of unpaid bills due within the specified time window
     */
    public List<Bill> getUpcomingBills(Long userId, int days) {
        int currentDay = LocalDate.now().getDayOfMonth();
        int lastDayToCheck = currentDay + days;
        int daysInMonth = LocalDate.now().lengthOfMonth();
        
        List<Bill> userBills = billRepository.findByUserId(userId);
        return userBills.stream()
                // Only include unpaid bills
                .filter(bill -> !bill.isPaid())
                // Check if bill is due within the specified time window
                .filter(bill -> {
                    int dueDay = bill.getDueDay();
                    // Either due in current month within window
                    return (dueDay >= currentDay && dueDay <= lastDayToCheck) ||
                           // Or due in next month's early days (when window spans months)
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