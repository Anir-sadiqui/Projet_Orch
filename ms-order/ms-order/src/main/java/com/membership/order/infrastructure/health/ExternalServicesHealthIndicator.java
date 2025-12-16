package com.membership.order.infrastructure.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Component("externalServices")
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private static final Logger logger =
            LoggerFactory.getLogger(ExternalServicesHealthIndicator.class);

    private static final String USER_HEALTH_URL =
            "http://localhost:8080/actuator/health";

    private static final String PRODUCT_HEALTH_URL =
            "http://localhost:8082/actuator/health";

    private final RestTemplate restTemplate;

    public ExternalServicesHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {

        boolean userServiceUp = isServiceUp(USER_HEALTH_URL, "User");
        boolean productServiceUp = isServiceUp(PRODUCT_HEALTH_URL, "Product");

        if (userServiceUp && productServiceUp) {
            return Health.up()
                    .withDetail("userService", "UP")
                    .withDetail("productService", "UP")
                    .build();
        }

        return Health.down()
                .withDetail("userService", userServiceUp ? "UP" : "DOWN")
                .withDetail("productService", productServiceUp ? "UP" : "DOWN")
                .build();
    }

    private boolean isServiceUp(String url, String serviceName) {
        try {
            restTemplate.getForObject(url, String.class);
            logger.debug("{} service UP", serviceName);
            return true;
        } catch (RestClientException e) {
            logger.warn("{} service DOWN", serviceName);
            return false;
        }
    }
}
