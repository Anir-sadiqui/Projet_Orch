package com.membership.order.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client HTTP pour communiquer avec le service Product (ms-product).
 * Responsable de vérifier l'existence et la disponibilité des produits.
 */
@Component
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082";

    private final RestTemplate restTemplate;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Récupère les détails d'un produit par son ID.
     * 
     * @param productId l'identifiant du produit
     * @return les détails du produit
     * @throws RuntimeException si le service Product est indisponible
     */
    public Map<String, Object> getProductById(Long productId) {
        try {
            String url = PRODUCT_SERVICE_URL + "/api/v1/products/" + productId;
            logger.info("Appel Product Service: GET {}", url);
            return restTemplate.getForObject(url, Map.class);
        } catch (RestClientException e) {
            logger.error("Erreur lors de l'appel au service Product pour le produit {}", productId, e);
            throw new RuntimeException("Service Product indisponible ou produit non trouvé: " + productId, e);
        }
    }

    /**
     * Vérifie si un produit existe.
     * 
     * @param productId l'identifiant du produit
     * @return true si le produit existe, false sinon
     */
    public boolean productExists(Long productId) {
        try {
            getProductById(productId);
            return true;
        } catch (Exception e) {
            logger.warn("Produit {} n'existe pas ou service indisponible", productId);
            return false;
        }
    }

    /**
     * Vérifie si un produit a suffisamment de stock.
     * 
     * @param productId l'identifiant du produit
     * @param requiredQuantity la quantité requise
     * @return true si le stock est suffisant, false sinon
     */
    public boolean hasEnoughStock(Long productId, Integer requiredQuantity) {
        try {
            Map<String, Object> product = getProductById(productId);
            Integer stock = (Integer) product.get("stock");
            return stock != null && stock >= requiredQuantity;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du stock pour le produit {}", productId, e);
            return false;
        }
    }

    /**
     * Met à jour le stock d'un produit.
     * 
     * @param productId l'identifiant du produit
     * @param quantityChange la quantité à ajouter ou retirer (négatif pour retirer)
     * @throws RuntimeException si la mise à jour échoue
     */
    public void updateStock(Long productId, Integer quantityChange) {
        try {
            String url = PRODUCT_SERVICE_URL + "/api/v1/products/" + productId + "/stock";
            
            Map<String, Object> request = new HashMap<>();
            request.put("quantityChange", quantityChange);
            
            logger.info("Appel Product Service: PATCH {} avec quantité {}", url, quantityChange);
            restTemplate.patchForObject(url, request, Map.class);
        } catch (RestClientException e) {
            logger.error("Erreur lors de la mise à jour du stock du produit {}", productId, e);
            throw new RuntimeException("Erreur lors de la mise à jour du stock: " + productId, e);
        }
    }
}
