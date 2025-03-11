package com.jay.home.finmanapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinManAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinManAppApplication.class, args);
    }
}