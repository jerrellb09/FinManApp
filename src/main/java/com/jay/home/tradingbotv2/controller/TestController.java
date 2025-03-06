package com.jay.home.tradingbotv2.controller;

import com.jay.home.tradingbotv2.dto.BillDTO;
import com.jay.home.tradingbotv2.mapper.BillMapper;
import com.jay.home.tradingbotv2.model.Bill;
import com.jay.home.tradingbotv2.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final BillMapper billMapper;
    private final BillService billService;

    @Autowired
    public TestController(BillMapper billMapper, BillService billService) {
        this.billMapper = billMapper;
        this.billService = billService;
    }

    @GetMapping("/bills")
    public ResponseEntity<List<Bill>> getAllBills() {
        List<Bill> bills = billService.getUserBills(1L);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/bills/simple")
    public ResponseEntity<List<BillDTO>> getAllBillsSimple() {
        List<Bill> bills = billService.getUserBills(1L);
        List<BillDTO> billDTOs = billMapper.toDTOList(bills);
        return ResponseEntity.ok(billDTOs);
    }
}