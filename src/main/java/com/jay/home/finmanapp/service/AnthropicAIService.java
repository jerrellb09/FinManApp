package com.jay.home.finmanapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnthropicAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnthropicAIService.class);
    
    @Value("${anthropic.api.key}")
    private String apiKey;
    
    @Value("${anthropic.api.url}")
    private String apiUrl;
    
    @Value("${anthropic.model}")
    private String model;
    
    @Value("${anthropic.max-tokens}")
    private int maxTokens;
    
    @Value("${anthropic.version}")
    private String apiVersion;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AnthropicAIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    public String generateAnalysis(String prompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Anthropic API key not configured");
            throw new RuntimeException("Anthropic API key not configured");
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", apiVersion);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            logger.info("Sending request to Anthropic API with model: {}", model);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                JsonNode content = responseJson.path("content");
                
                if (content.isArray() && content.size() > 0) {
                    return content.get(0).path("text").asText();
                } else {
                    logger.warn("Unexpected response format from Anthropic API");
                    return "Unable to generate analysis due to unexpected response format.";
                }
            } else {
                logger.error("Anthropic API request failed with status: {}", response.getStatusCode());
                throw new RuntimeException("Anthropic API request failed: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error calling Anthropic API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate AI analysis: " + e.getMessage(), e);
        }
    }
    
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}