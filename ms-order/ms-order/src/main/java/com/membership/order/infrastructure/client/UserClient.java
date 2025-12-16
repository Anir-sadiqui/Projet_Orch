package com.membership.order.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Component
public class UserClient {

    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);
    private static final String USER_SERVICE_URL = "http://localhost:8080";

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean userExists(Long userId) {
        try {
            String url = USER_SERVICE_URL + "/api/v1/users/" + userId;
            logger.info("GET {}", url);
            restTemplate.getForObject(url, Void.class);
            return true;
        } catch (RestClientException e) {
            logger.warn("Utilisateur {} introuvable ou service User indisponible", userId);
            return false;
        }
    }
}
