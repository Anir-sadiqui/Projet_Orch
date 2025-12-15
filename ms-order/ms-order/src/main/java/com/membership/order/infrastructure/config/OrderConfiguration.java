package com.membership.order.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration globale pour le service Order.
 * Définit les beans partagés comme RestTemplate.
 */
@Configuration
public class OrderConfiguration {

    /**
     * Crée un bean RestTemplate pour les appels inter-services.
     * 
     * @param builder le builder fourni par Spring
     * @return une instance configurée de RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))  // Timeout de connexion: 5 secondes
            .setReadTimeout(Duration.ofSeconds(5))     // Timeout de lecture: 5 secondes
            .build();
    }
}
