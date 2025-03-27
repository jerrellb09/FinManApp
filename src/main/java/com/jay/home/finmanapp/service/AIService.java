package com.jay.home.finmanapp.service;

import com.jay.home.finmanapp.model.Budget;
import com.jay.home.finmanapp.model.Transaction;
import com.jay.home.finmanapp.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${llama3.api.url:http://localhost:8081}")
    private String llama3ApiUrl;

    private final RestTemplate restTemplate;
    private final TransactionService transactionService;
    private final BudgetService budgetService;

    public AIService(RestTemplate restTemplate, TransactionService transactionService, BudgetService budgetService) {
        this.restTemplate = restTemplate;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
    }

    /**
     * Generate financial insights based on user transaction data
     * @param user The user to generate insights for
     * @return A map containing the generated insights
     */
    public Map<String, Object> generateFinancialInsights(User user) {
        List<Transaction> recentTransactions = new ArrayList<>();
        try {
            recentTransactions = transactionService.getRecentTransactionsForUser(user, 30);
        } catch (Exception e) {
            // For testing purposes, if getting transactions fails, use an empty list
            recentTransactions = new ArrayList<>();
        }
        
        String prompt = buildInsightPrompt(user, recentTransactions);
        String aiResponse = callLlama3Api(prompt);
        
        Map<String, Object> result = new HashMap<>();
        result.put("insights", aiResponse);
        return result;
    }

    /**
     * Generate suggested budgets based on user spending patterns
     * @param user The user to generate budget suggestions for
     * @return A map containing the suggested budgets
     */
    public Map<String, Object> generateBudgetSuggestions(User user) {
        List<Transaction> transactions = transactionService.getRecentTransactionsForUser(user, 90);
        List<Budget> existingBudgets = budgetService.getBudgetsByUser(user);
        
        String prompt = buildBudgetPrompt(user, transactions, existingBudgets);
        String aiResponse = callLlama3Api(prompt);
        
        Map<String, Object> result = new HashMap<>();
        result.put("suggestions", aiResponse);
        return result;
    }

    /**
     * Generate spending habit analysis with charts description
     * @param user The user to analyze spending habits for
     * @return A map containing the spending habit analysis
     */
    public Map<String, Object> analyzeSpendingHabits(User user) {
        List<Transaction> transactions = transactionService.getRecentTransactionsForUser(user, 60);
        
        String prompt = buildSpendingHabitsPrompt(user, transactions);
        String aiResponse = callLlama3Api(prompt);
        
        Map<String, Object> result = new HashMap<>();
        result.put("analysis", aiResponse);
        result.put("chartSuggestions", extractChartSuggestions(aiResponse));
        return result;
    }

    /**
     * Call the LLaMA 3 API with a prompt
     * @param prompt The prompt to send to the LLaMA 3 model
     * @return The model's response
     */
    private String callLlama3Api(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(
                llama3ApiUrl + "/api/generate", 
                request, 
                Map.class
            );
            
            if (response != null && response.containsKey("response")) {
                return (String) response.get("response");
            }
            return generateFallbackResponse(prompt);
        } catch (Exception e) {
            return generateFallbackResponse(prompt);
        }
    }
    
    /**
     * Generate a fallback response when the AI service is unavailable
     * @param prompt The original prompt
     * @return A fallback response
     */
    private String generateFallbackResponse(String prompt) {
        if (prompt.contains("financial insights")) {
            return "Based on your recent transactions, here are some financial insights:\n\n" +
                   "1. Your spending in the Food category appears to be higher than average. Consider setting a budget for eating out.\n\n" +
                   "2. You have several recurring subscription services. Review these regularly to ensure you're using them all.\n\n" +
                   "3. Setting aside 10-15% of your income for savings would help build an emergency fund.\n\n" +
                   "Note: This is a fallback response as the AI service is currently unavailable. Please try again later for personalized insights.";
        } else if (prompt.contains("budget")) {
            return "Here are some suggested budget allocations based on standard financial guidelines:\n\n" +
                   "Housing: $1,500 - This follows the 30% rule for housing expenses based on your income.\n\n" +
                   "Food: $500 - Allocate about 10% of your monthly income for groceries and dining out.\n\n" +
                   "Transportation: $400 - This covers typical costs for fuel, maintenance, and public transit.\n\n" +
                   "Savings: $750 - Try to save at least 15% of your income each month.\n\n" +
                   "Note: This is a fallback response as the AI service is currently unavailable. Please try again later for personalized budget suggestions.";
        } else {
            return "Here's an analysis of typical spending patterns:\n\n" +
                   "1. Weekend spending is usually 30% higher than weekday spending for most people.\n\n" +
                   "2. The largest spending categories for most households are Housing (30%), Food (15%), and Transportation (10%).\n\n" +
                   "3. Many people don't realize how much they spend on subscription services, which can add up to 5-8% of monthly expenses.\n\n" +
                   "CHART_SUGGESTION: A pie chart showing the breakdown of spending by category would help visualize where most money is going.\n\n" +
                   "CHART_SUGGESTION: A line chart tracking spending over time can reveal seasonal patterns and trends.\n\n" +
                   "CHART_SUGGESTION: A bar chart comparing actual spending against budgeted amounts helps identify areas that need attention.\n\n" +
                   "Note: This is a fallback response as the AI service is currently unavailable. Please try again later for personalized analysis.";
        }
    }

    /**
     * Build a prompt for generating financial insights
     */
    private String buildInsightPrompt(User user, List<Transaction> transactions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a financial advisor analyzing transaction data. ");
        prompt.append("Based on the following transactions, provide 3-5 personalized financial insights and recommendations:\n\n");
        
        // Add user info
        prompt.append("Monthly Income: $").append(user.getMonthlyIncome()).append("\n");
        
        // Add transaction data
        prompt.append("Recent Transactions:\n");
        for (Transaction transaction : transactions) {
            prompt.append("- $").append(transaction.getAmount())
                  .append(" for ").append(transaction.getDescription())
                  .append(" on ").append(transaction.getDate())
                  .append(" (Category: ").append(transaction.getCategory().getName())
                  .append(")\n");
        }
        
        prompt.append("\nProvide actionable financial insights formatted in paragraphs. Focus on spending patterns, saving opportunities, and budgeting advice.");
        
        return prompt.toString();
    }

    /**
     * Build a prompt for generating budget suggestions
     */
    private String buildBudgetPrompt(User user, List<Transaction> transactions, List<Budget> existingBudgets) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a financial advisor creating budget recommendations. ");
        prompt.append("Based on the following information, suggest appropriate monthly budget amounts for different spending categories:\n\n");
        
        // Add user info
        prompt.append("Monthly Income: $").append(user.getMonthlyIncome()).append("\n");
        
        // Add existing budgets
        prompt.append("Current Budgets:\n");
        for (Budget budget : existingBudgets) {
            prompt.append("- ").append(budget.getCategory().getName())
                  .append(": $").append(budget.getAmount()).append("\n");
        }
        
        // Summarize transactions by category
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory().getName();
            categoryTotals.put(category, 
                categoryTotals.getOrDefault(category, 0.0) + transaction.getAmount().doubleValue());
        }
        
        prompt.append("\nRecent 3-Month Spending by Category:\n");
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            prompt.append("- ").append(entry.getKey())
                  .append(": $").append(String.format("%.2f", entry.getValue() / 3)) // Monthly average
                  .append(" per month\n");
        }
        
        prompt.append("\nProvide budget recommendations for each major spending category in the format 'Category: $Amount'. ");
        prompt.append("Include a brief explanation for each recommendation. Focus on creating a balanced budget that allows for saving at least 15-20% of income.");
        
        return prompt.toString();
    }

    /**
     * Build a prompt for analyzing spending habits
     */
    private String buildSpendingHabitsPrompt(User user, List<Transaction> transactions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a financial data analyst examining spending patterns. ");
        prompt.append("Based on the following transaction data, analyze spending habits and suggest helpful charts to visualize patterns:\n\n");
        
        // Add user info
        prompt.append("Monthly Income: $").append(user.getMonthlyIncome()).append("\n");
        
        // Add transaction data
        prompt.append("Transactions from the past 60 days:\n");
        for (Transaction transaction : transactions) {
            prompt.append("- $").append(transaction.getAmount())
                  .append(" for ").append(transaction.getDescription())
                  .append(" on ").append(transaction.getDate())
                  .append(" (Category: ").append(transaction.getCategory().getName())
                  .append(")\n");
        }
        
        prompt.append("\nProvide a detailed analysis of spending habits, including:\n");
        prompt.append("1. Top spending categories and percentage of total spending\n");
        prompt.append("2. Recurring transactions and patterns\n");
        prompt.append("3. Weekend vs weekday spending\n");
        prompt.append("4. Unusual or potentially problematic spending patterns\n\n");
        
        prompt.append("Then, suggest 3 specific charts/visualizations that would help understand these patterns better.");
        prompt.append("For each chart, describe the chart type, what data it would display, and what insights it might reveal.");
        prompt.append("Format chart suggestions as 'CHART_SUGGESTION: {chart description}'");
        
        return prompt.toString();
    }

    /**
     * Extract chart suggestions from AI response
     */
    private Map<String, String> extractChartSuggestions(String aiResponse) {
        Map<String, String> chartSuggestions = new HashMap<>();
        String[] lines = aiResponse.split("\n");
        
        int chartNumber = 1;
        for (String line : lines) {
            if (line.contains("CHART_SUGGESTION:")) {
                String chartDescription = line.substring(line.indexOf(":") + 1).trim();
                chartSuggestions.put("chart" + chartNumber, chartDescription);
                chartNumber++;
            }
        }
        
        return chartSuggestions;
    }
}