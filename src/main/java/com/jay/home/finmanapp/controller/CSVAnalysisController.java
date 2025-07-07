package com.jay.home.finmanapp.controller;

import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.service.CSVAnalysisService;
import com.jay.home.finmanapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/csv-analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CSV Analysis", description = "API for analyzing imported CSV data with AI")
public class CSVAnalysisController {

    private final CSVAnalysisService csvAnalysisService;
    private final UserService userService;

    @GetMapping("/import-insights")
    @Operation(
        summary = "Get AI insights for recently imported CSV data",
        description = "Analyzes recently imported financial data and provides AI-powered insights, " +
                     "spending patterns, and recommendations"
    )
    public ResponseEntity<Map<String, Object>> getImportInsights() {
        try {
            // Use demo user for now (same as CSV import)
            User user = userService.findByEmail("demo@finmanapp.com");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User not found"));
            }
            
            Map<String, Object> insights = csvAnalysisService.analyzeImportedData(user);
            return ResponseEntity.ok(insights);
            
        } catch (Exception e) {
            log.error("Error generating import insights", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to generate insights: " + e.getMessage()));
        }
    }
    
    @GetMapping("/spending-patterns")
    @Operation(
        summary = "Analyze spending patterns from imported data",
        description = "Identifies spending patterns, trends, and anomalies in imported transaction data"
    )
    public ResponseEntity<Map<String, Object>> getSpendingPatterns() {
        try {
            User user = userService.findByEmail("demo@finmanapp.com");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User not found"));
            }
            
            Map<String, Object> patterns = csvAnalysisService.analyzeSpendingPatterns(user);
            return ResponseEntity.ok(patterns);
            
        } catch (Exception e) {
            log.error("Error analyzing spending patterns", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to analyze patterns: " + e.getMessage()));
        }
    }
    
    @GetMapping("/financial-health")
    @Operation(
        summary = "Assess financial health based on imported data",
        description = "Provides a comprehensive financial health assessment with recommendations"
    )
    public ResponseEntity<Map<String, Object>> getFinancialHealthAssessment() {
        try {
            User user = userService.findByEmail("demo@finmanapp.com");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User not found"));
            }
            
            Map<String, Object> assessment = csvAnalysisService.assessFinancialHealth(user);
            return ResponseEntity.ok(assessment);
            
        } catch (Exception e) {
            log.error("Error assessing financial health", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to assess financial health: " + e.getMessage()));
        }
    }
    
    @GetMapping("/bill-optimization")
    @Operation(
        summary = "Get AI recommendations for bill optimization",
        description = "Analyzes detected bills and provides optimization recommendations"
    )
    public ResponseEntity<Map<String, Object>> getBillOptimization() {
        try {
            User user = userService.findByEmail("demo@finmanapp.com");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User not found"));
            }
            
            Map<String, Object> optimization = csvAnalysisService.analyzeBillOptimization(user);
            return ResponseEntity.ok(optimization);
            
        } catch (Exception e) {
            log.error("Error analyzing bill optimization", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to analyze bill optimization: " + e.getMessage()));
        }
    }
}