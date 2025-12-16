package com.membership.order.infrastructure.client;

import com.membership.order.infrastructure.client.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082";

    private final RestTemplate restTemplate;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public ProductDTO getProduct(Long productId) {
        try {
            String url = PRODUCT_SERVICE_URL + "/api/v1/products/" + productId;
            logger.info("GET {}", url);
            return restTemplate.getForObject(url, ProductDTO.class);
        } catch (RestClientException e) {
            logger.error("Produit {} introuvable ou service Product indisponible", productId, e);
            throw new RuntimeException(
                    "Produit introuvable ou service Product indisponible: " + productId
            );
        }
    }


    public void updateStock(Long productId, Integer newStock) {
        try {
            String url = PRODUCT_SERVICE_URL + "/api/v1/products/" + productId + "/stock";

            Map<String, Object> request = new HashMap<>();
            request.put("quantityChange", newStock);

            logger.info("PATCH {} newStock={}", url, newStock);
            restTemplate.patchForObject(url, request, Void.class);

        } catch (RestClientException e) {
            logger.error("Erreur mise à jour stock produit {}", productId, e);
            throw new RuntimeException(
                    "Erreur mise à jour stock pour le produit " + productId
            );
        }
    }
}
