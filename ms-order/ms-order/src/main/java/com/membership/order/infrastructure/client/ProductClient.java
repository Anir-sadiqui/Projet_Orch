package com.membership.order.infrastructure.client;

import com.membership.order.infrastructure.client.dto.ProductDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProductClient {

    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082";
    private final RestTemplate restTemplate;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductDTO getProduct(Long productId) {
        String url = PRODUCT_SERVICE_URL + "/api/v1/products/" + productId;
        return restTemplate.getForObject(url, ProductDTO.class);
    }

    public void updateStock(Long productId, int quantityChange) {

        String url = PRODUCT_SERVICE_URL + "/api/v1/products/" + productId + "/stock";

        Map<String, Integer> body = new HashMap<>();
        body.put("quantityChange", quantityChange);

        restTemplate.put(url, body);
    }


}
