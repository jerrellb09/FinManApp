package com.jay.home.finmanapp.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenTelemetry observability integration.
 * Uses Spring Boot OpenTelemetry auto-configuration for simplified setup.
 */
@Configuration
public class ObservabilityConfig {

    /**
     * Provides a tracer instance for the application
     * Uses the auto-configured OpenTelemetry instance from Spring Boot
     */
    @Bean
    public Tracer tracer(@Autowired OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("finmanapp", "1.0.0");
    }
}