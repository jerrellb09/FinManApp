package com.jay.home.finmanapp.controller;

import com.jay.home.finmanapp.dto.BillDTO;
import com.jay.home.finmanapp.mapper.BillMapper;
import com.jay.home.finmanapp.model.Bill;
import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.service.AIService;
import com.jay.home.finmanapp.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final BillMapper billMapper;
    private final BillService billService;
    private final ApplicationContext applicationContext;

    @Autowired
    public TestController(BillMapper billMapper, BillService billService, ApplicationContext applicationContext) {
        this.billMapper = billMapper;
        this.billService = billService;
        this.applicationContext = applicationContext;
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
    
    @GetMapping("/ai-status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "backend_running");
        response.put("message", "The backend API is working. Your frontend should call the /api/insights/ai/* endpoints, not /generate directly.");
        response.put("aiStatus", "The LLaMA 3 API is available at port 8081 via Ollama.");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ai-test")
    public ResponseEntity<Map<String, Object>> testAIEndpoint() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Create a simplified user for testing
            User testUser = new User();
            testUser.setEmail("test@example.com");
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setMonthlyIncome(new BigDecimal("5000.00"));
            
            // Get AI insights
            AIService aiService = applicationContext.getBean(AIService.class);
            Map<String, Object> insights = aiService.generateFinancialInsights(testUser);
            
            result.put("success", true);
            result.put("insights", insights);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}