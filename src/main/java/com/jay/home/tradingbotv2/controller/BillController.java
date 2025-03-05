package com.jay.home.tradingbotv2.controller;

import com.jay.home.tradingbotv2.model.Bill;
import com.jay.home.tradingbotv2.service.BillService;
import com.jay.home.tradingbotv2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")

public class BillController {

    private final BillService billService;
    private final UserService userService;

    @Autowired
    public BillController(BillService billService, UserService userService) {
        this.billService = billService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill, @RequestParam Long userId) {
        Bill createdBill = billService.createBill(bill, userId);
        return new ResponseEntity<>(createdBill, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bill>> getUserBills(@PathVariable Long userId) {
        List<Bill> bills = billService.getUserBills(userId);
        return new ResponseEntity<>(bills, HttpStatus.OK);
    }

    @GetMapping("/due/{userId}")
    public ResponseEntity<List<Bill>> getDueBills(@PathVariable Long userId) {
        List<Bill> dueBills = billService.getDueBills(userId);
        return new ResponseEntity<>(dueBills, HttpStatus.OK);
    }

    @PutMapping("/{billId}")
    public ResponseEntity<Bill> updateBill(@PathVariable Long billId, @RequestBody Bill billDetails) {
        Bill updatedBill = billService.updateBill(billId, billDetails);
        return new ResponseEntity<>(updatedBill, HttpStatus.OK);
    }

    @DeleteMapping("/{billId}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long billId) {
        billService.deleteBill(billId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{billId}/pay")
    public ResponseEntity<Void> markBillAsPaid(@PathVariable Long billId) {
        billService.markBillAsPaid(billId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/remaining-income/{userId}")
    public ResponseEntity<Map<String, BigDecimal>> getRemainingIncome(@PathVariable Long userId) {
        BigDecimal remainingIncome = billService.getRemainingIncome(userId);
        return new ResponseEntity<>(Map.of("remainingIncome", remainingIncome), HttpStatus.OK);
    }
    
    @PostMapping("/reset-monthly/{userId}")
    public ResponseEntity<Void> resetMonthlyBills(@PathVariable Long userId) {
        billService.resetMonthlyBills(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @GetMapping("/by-category")
    public ResponseEntity<Map<String, List<Bill>>> getBillsByCategory(@RequestParam Long userId) {
        Map<String, List<Bill>> billsByCategory = billService.getBillsByCategory(userId);
        return new ResponseEntity<>(billsByCategory, HttpStatus.OK);
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<Bill>> getUpcomingBills(@RequestParam Long userId, @RequestParam(defaultValue = "30") int days) {
        List<Bill> upcomingBills = billService.getUpcomingBills(userId, days);
        return new ResponseEntity<>(upcomingBills, HttpStatus.OK);
    }
    
    @GetMapping("/monthly-total")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyTotal(@RequestParam Long userId) {
        BigDecimal monthlyTotal = billService.getMonthlyBillsTotal(userId);
        return new ResponseEntity<>(Map.of("monthlyTotal", monthlyTotal), HttpStatus.OK);
    }
}