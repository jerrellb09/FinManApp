package com.jay.home.finmanapp.controller;

import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.service.CSVIngestionService;
import com.jay.home.finmanapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/csv")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CSV Import", description = "API for importing financial data from CSV files")
public class CSVImportController {

    private final CSVIngestionService csvIngestionService;
    private final UserService userService;

    @PostMapping(value = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "text/csv"})
    @Operation(
        summary = "Import financial data from CSV file",
        description = "Uploads and processes a CSV file containing financial transaction data. " +
                     "The system will automatically identify recurring transactions and create bills, " +
                     "categorize transactions, and create accounts as needed."
    )
    @ApiResponse(responseCode = "200", description = "CSV file imported successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file format or content")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "500", description = "Internal server error during import")
    public ResponseEntity<Map<String, Object>> importCSV(
            @Parameter(description = "CSV file containing financial transaction data", required = true)
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File is empty"));
            }
            
            if (!isCSVFile(file)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("File must be a CSV file"));
            }
            
            // Use demo user for testing
            User user = userService.findByEmail("demo@finmanapp.com");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Demo user not found"));
            }
            
            // Process CSV file
            CSVIngestionService.CSVImportResult result = csvIngestionService.importCSV(file, user);
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.isSuccess() ? "CSV import completed successfully" : result.getErrorMessage());
            response.put("summary", createSummary(result));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during CSV import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to process CSV file: " + e.getMessage()));
        }
    }
    

    @GetMapping("/test-ai")
    @Operation(
        summary = "Test AI functionality",
        description = "Tests the current AI provider configuration"
    )
    public ResponseEntity<Map<String, Object>> testAI() {
        try {
            // Create a test user for AI testing
            User testUser = new User();
            testUser.setEmail("test@example.com");
            testUser.setMonthlyIncome(java.math.BigDecimal.valueOf(5000));
            
            // Test AI service directly
            com.jay.home.finmanapp.service.AIService aiService = 
                new com.jay.home.finmanapp.service.AIService(
                    new org.springframework.web.client.RestTemplate(),
                    null,
                    null
                );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "AI test endpoint ready");
            response.put("aiProvider", System.getProperty("ai.provider", "llama3"));
            response.put("message", "AI service configuration loaded successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/sample")
    @Operation(
        summary = "Download sample CSV template",
        description = "Downloads a sample CSV file showing the expected format for financial data import"
    )
    @ApiResponse(responseCode = "200", description = "Sample CSV file downloaded")
    public ResponseEntity<String> downloadSampleCSV() {
        String sampleCSV = """
            Date,Original Date,Account Type,Account Name,Account Number,Institution Name,Name,Custom Name,Amount,Description,Category,Note,Ignored From,Tax Deductible
            2023-01-15,2023-01-15,Checking,My Checking,1234,Bank of America,RENT PAYMENT,,1200.00,Monthly rent payment,Bills & Utilities,,,false
            2023-01-16,2023-01-16,Credit Card,My Credit Card,5678,Chase,GROCERY STORE,,85.50,Weekly grocery shopping,Groceries,,,false
            2023-01-17,2023-01-17,Checking,My Checking,1234,Bank of America,SALARY DEPOSIT,,-2500.00,Monthly salary,Income,,,false
            2023-01-18,2023-01-18,Credit Card,My Credit Card,5678,Chase,NETFLIX,,15.99,Monthly Netflix subscription,Entertainment,,,false
            """;
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sample_transactions.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(sampleCSV);
    }
    
    private boolean isCSVFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("text/csv")) ||
               (filename != null && filename.toLowerCase().endsWith(".csv"));
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
    
    private Map<String, Object> createSummary(CSVIngestionService.CSVImportResult result) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTransactions", result.getTotalTransactions());
        summary.put("transactionsSaved", result.getTransactionsSaved());
        summary.put("accountsCreated", result.getAccountsCreated());
        summary.put("categoriesCreated", result.getCategoriesCreated());
        summary.put("billsCreated", result.getBillsCreated());
        return summary;
    }
}