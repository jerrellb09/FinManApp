package com.jay.home.finmanapp.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer customJackson() {
        return builder -> builder
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // We mainly rely on DTOs and JsonManagedReference/JsonBackReference annotations to solve
        // the circular reference issue instead of increasing the nesting depth limit
    }
}
