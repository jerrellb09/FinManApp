package com.jay.home.finmanapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {
    
    private String provider = "llama3";
    private boolean fallbackEnabled = true;
    
    // Getters and setters
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }
    
    public void setFallbackEnabled(boolean fallbackEnabled) {
        this.fallbackEnabled = fallbackEnabled;
    }
}