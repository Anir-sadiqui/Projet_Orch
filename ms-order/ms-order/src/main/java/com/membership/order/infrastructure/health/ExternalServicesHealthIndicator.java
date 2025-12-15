package com.membership.order.infrastructure.health;

import com.membership.order.infrastructure.client.ProductClient;
import com.membership.order.infrastructure.client.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Indicateur de santé personnalisé pour vérifier la disponibilité des services externes.
 * Vérifie la disponibilité des services User et Product.
 */
@Component("externalServices")
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServicesHealthIndicator.class);

    private final UserClient userClient;
    private final ProductClient productClient;

    public ExternalServicesHealthIndicator(UserClient userClient, ProductClient productClient) {
        this.userClient = userClient;
        this.productClient = productClient;
    }

    @Override
    public Health health() {
        try {
            // Tester User Service avec un utilisateur de test (ID = 1)
            boolean userServiceUp = checkUserService();
            
            // Tester Product Service avec un produit de test (ID = 1)
            boolean productServiceUp = checkProductService();

            if (userServiceUp && productServiceUp) {
                return Health.up()
                    .withDetail("userService", "UP")
                    .withDetail("productService", "UP")
                    .withDetail("message", "Tous les services externes sont accessibles")
                    .build();
            } else {
                return Health.down()
                    .withDetail("userService", userServiceUp ? "UP" : "DOWN")
                    .withDetail("productService", productServiceUp ? "UP" : "DOWN")
                    .withDetail("message", "Un ou plusieurs services externes sont indisponibles")
                    .build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des services externes", e);
            return Health.down()
                .withDetail("error", "Erreur lors de la vérification des services")
                .withException(e)
                .build();
        }
    }

    /**
     * Vérifie la disponibilité du service User.
     * 
     * @return true si le service est accessible
     */
    private boolean checkUserService() {
        try {
            // Essayer de récupérer un utilisateur (test basique de connectivité)
            userClient.userExists(1L);
            logger.debug("Service User accessible");
            return true;
        } catch (Exception e) {
            logger.warn("Service User indisponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie la disponibilité du service Product.
     * 
     * @return true si le service est accessible
     */
    private boolean checkProductService() {
        try {
            // Essayer de récupérer un produit (test basique de connectivité)
            productClient.productExists(1L);
            logger.debug("Service Product accessible");
            return true;
        } catch (Exception e) {
            logger.warn("Service Product indisponible: {}", e.getMessage());
            return false;
        }
    }
}
