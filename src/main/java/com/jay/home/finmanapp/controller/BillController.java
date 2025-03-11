package com.jay.home.finmanapp.controller;

import com.jay.home.finmanapp.dto.BillDTO;
import com.jay.home.finmanapp.mapper.BillMapper;
import com.jay.home.finmanapp.model.Bill;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.service.BillService;
import com.jay.home.finmanapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;
    private final UserService userService;
    private final BillMapper billMapper;

    @Autowired
    public BillController(BillService billService, UserService userService, BillMapper billMapper) {
        this.billService = billService;
        this.userService = userService;
        this.billMapper = billMapper;
    }

    @PostMapping
    public ResponseEntity<BillDTO> createBill(
            @AuthenticationPrincipal String userEmail,
            @RequestBody Bill bill, 
            @RequestParam Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is creating a bill for themselves
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        bill.setUser(user);
        Bill createdBill = billService.createBill(bill, userId);
        
        // Convert to DTO to avoid circular references
        BillDTO createdBillDTO = billMapper.toDTO(createdBill);
        
        return new ResponseEntity<>(createdBillDTO, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bill>> getUserBills(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Bill> bills = billService.getUserBills(userId);
        return new ResponseEntity<>(bills, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/simple")
    public ResponseEntity<List<BillDTO>> getUserBillsSimple(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Bill> bills = billService.getUserBills(userId);
        List<BillDTO> billDTOs = billMapper.toDTOList(bills);
        return new ResponseEntity<>(billDTOs, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/ids")
    public ResponseEntity<List<Long>> getUserBillIds(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Bill> bills = billService.getUserBills(userId);
        List<Long> billIds = bills.stream()
            .map(Bill::getId)
            .collect(java.util.stream.Collectors.toList());
        return new ResponseEntity<>(billIds, HttpStatus.OK);
    }
    
    @GetMapping("/{billId}")
    public ResponseEntity<BillDTO> getBillById(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long billId) {
        
        User user = userService.getUserByEmail(userEmail);
        Bill bill = billService.getBillById(billId);
        
        // Check if the bill exists and belongs to the authenticated user
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!bill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Convert to DTO to avoid circular references
        BillDTO billDTO = billMapper.toDTO(bill);
        return new ResponseEntity<>(billDTO, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/category/{categoryId}")
    public ResponseEntity<List<Bill>> getUserBillsByCategory(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId,
            @PathVariable Long categoryId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Bill> bills = billService.getUserBillsByCategory(userId, categoryId);
        return new ResponseEntity<>(bills, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/category/{categoryId}/simple")
    public ResponseEntity<List<BillDTO>> getUserBillsByCategorySimple(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId,
            @PathVariable Long categoryId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Bill> bills = billService.getUserBillsByCategory(userId, categoryId);
        List<BillDTO> billDTOs = billMapper.toDTOList(bills);
        return new ResponseEntity<>(billDTOs, HttpStatus.OK);
    }

    @GetMapping("/due/{userId}")
    public ResponseEntity<List<Bill>> getDueBills(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own due bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Bill> dueBills = billService.getDueBills(userId);
        return new ResponseEntity<>(dueBills, HttpStatus.OK);
    }
    
    @GetMapping("/due/{userId}/simple")
    public ResponseEntity<List<BillDTO>> getDueBillsSimple(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own due bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Bill> dueBills = billService.getDueBills(userId);
        List<BillDTO> dueBillDTOs = billMapper.toDTOList(dueBills);
        return new ResponseEntity<>(dueBillDTOs, HttpStatus.OK);
    }

    @PutMapping("/{billId}")
    public ResponseEntity<BillDTO> updateBill(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long billId, 
            @RequestBody Bill billDetails) {
        
        User user = userService.getUserByEmail(userEmail);
        Bill existingBill = billService.getBillById(billId);
        
        // Check if the bill exists and belongs to the authenticated user
        if (existingBill == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!existingBill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Bill updatedBill = billService.updateBill(billId, billDetails);
        
        // Convert to DTO to avoid circular references
        BillDTO updatedBillDTO = billMapper.toDTO(updatedBill);
        
        return new ResponseEntity<>(updatedBillDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{billId}")
    public ResponseEntity<Void> deleteBill(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long billId) {
        
        User user = userService.getUserByEmail(userEmail);
        Bill bill = billService.getBillById(billId);
        
        // Check if the bill exists and belongs to the authenticated user
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!bill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        billService.deleteBill(billId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{billId}/pay")
    public ResponseEntity<Void> markBillAsPaid(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long billId) {
        
        User user = userService.getUserByEmail(userEmail);
        Bill bill = billService.getBillById(billId);
        
        // Check if the bill exists and belongs to the authenticated user
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!bill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        billService.markBillAsPaid(billId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @PatchMapping("/{billId}/unpay")
    public ResponseEntity<Void> markBillAsUnpaid(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long billId) {
        
        User user = userService.getUserByEmail(userEmail);
        Bill bill = billService.getBillById(billId);
        
        // Check if the bill exists and belongs to the authenticated user
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!bill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        billService.markBillAsUnpaid(billId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/remaining-income/{userId}")
    public ResponseEntity<Map<String, BigDecimal>> getRemainingIncome(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own income info
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        BigDecimal remainingIncome = billService.getRemainingIncome(userId);
        return new ResponseEntity<>(Map.of("remainingIncome", remainingIncome), HttpStatus.OK);
    }
    
    @PostMapping("/reset-monthly/{userId}")
    public ResponseEntity<Void> resetMonthlyBills(
            @AuthenticationPrincipal String userEmail,
            @PathVariable Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is resetting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        billService.resetMonthlyBills(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @GetMapping("/by-category")
    public ResponseEntity<Map<String, List<Bill>>> getBillsByCategory(
            @AuthenticationPrincipal String userEmail,
            @RequestParam Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Map<String, List<Bill>> billsByCategory = billService.getBillsByCategory(userId);
        return new ResponseEntity<>(billsByCategory, HttpStatus.OK);
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<Bill>> getUpcomingBills(
            @AuthenticationPrincipal String userEmail,
            @RequestParam Long userId, 
            @RequestParam(defaultValue = "30") int days) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Bill> upcomingBills = billService.getUpcomingBills(userId, days);
        return new ResponseEntity<>(upcomingBills, HttpStatus.OK);
    }
    
    @GetMapping("/monthly-total")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyTotal(
            @AuthenticationPrincipal String userEmail,
            @RequestParam Long userId) {
        
        User user = userService.getUserByEmail(userEmail);
        
        // Check if the authenticated user is requesting their own bills
        if (!user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        BigDecimal monthlyTotal = billService.getMonthlyBillsTotal(userId);
        return new ResponseEntity<>(Map.of("monthlyTotal", monthlyTotal), HttpStatus.OK);
    }
}