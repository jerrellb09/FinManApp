package com.jay.home.finmanapp.config;

import com.jay.home.finmanapp.PlaidClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaidConfig {
    @Value("${plaid.client-id}")
    private String clientId;

    @Value("${plaid.secret}")
    private String secret;

    @Value("${plaid.environment}")
    private String environment;

    @Bean
    public PlaidClient plaidClient() {
        return new PlaidClient(clientId, secret, environment);
    }
}