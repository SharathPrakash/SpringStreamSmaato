package com.smaato.smaato.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Configurations {
    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}