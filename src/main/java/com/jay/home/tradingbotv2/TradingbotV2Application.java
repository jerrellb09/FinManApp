package com.jay.home.tradingbotv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradingbotV2Application {

    public static void main(String[] args) {
        SpringApplication.run(TradingbotV2Application.class, args);
    }
}
