package com.jay.home.finmanapp.config;

import com.jay.home.finmanapp.service.BillService;
import com.jay.home.finmanapp.service.BudgetService;
import com.jay.home.finmanapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class SchedulingConfig {
    private final UserService userService;
    private final BudgetService budgetService;
    private final BillService billService;

    @Autowired
    public SchedulingConfig(UserService userService, BudgetService budgetService, BillService billService) {
        this.userService = userService;
        this.budgetService = budgetService;
        this.billService = billService;
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void checkBudgetThresholds() {
        userService.getAllUsers().forEach(user -> budgetService.checkBudgetThresholds(user));
    }
    
    @Scheduled(cron = "0 0 0 1 * *") // Run at midnight on the first day of each month
    public void resetMonthlyBills() {
        userService.getAllUsers().forEach(user -> {
            billService.resetMonthlyBills(user.getId());
        });
    }
}