package com.membership.order.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client HTTP pour communiquer avec le service User (ms-membership).
 * Responsable de vérifier l'existence des utilisateurs.
 */
@Component
public class UserClient {

    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);
    private static final String USER_SERVICE_URL = "http://localhost:8080";

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Récupère un utilisateur par son ID.
     * 
     * @param userId l'identifiant de l'utilisateur
     * @return les détails de l'utilisateur
     * @throws RuntimeException si le service User est indisponible
     */
    public Map<String, Object> getUserById(Long userId) {
        try {
            String url = USER_SERVICE_URL + "/api/v1/users/" + userId;
            logger.info("Appel User Service: GET {}", url);
            return restTemplate.getForObject(url, Map.class);
        } catch (RestClientException e) {
            logger.error("Erreur lors de l'appel au service User pour l'utilisateur {}", userId, e);
            throw new RuntimeException("Service User indisponible ou utilisateur non trouvé: " + userId, e);
        }
    }

    /**
     * Vérifie si un utilisateur existe.
     * 
     * @param userId l'identifiant de l'utilisateur
     * @return true si l'utilisateur existe, false sinon
     */
    public boolean userExists(Long userId) {
        try {
            getUserById(userId);
            return true;
        } catch (Exception e) {
            logger.warn("Utilisateur {} n'existe pas ou service indisponible", userId);
            return false;
        }
    }
}
