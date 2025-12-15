package com.membership.product.infrastructure.health;

import com.membership.product.domain.repository.ProductRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Indicateur de santé personnalisé pour vérifier les niveaux de stock.
 * Avertit si des produits ont un stock inférieur à 5 unités.
 */
@Component("stockHealth")
public class StockHealthIndicator implements HealthIndicator {

    private final ProductRepository productRepository;
    private static final int LOW_STOCK_THRESHOLD = 5;

    public StockHealthIndicator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Health health() {
        try {
            long lowStockCount = productRepository.findAll().stream()
                .filter(p -> p.getStock() < LOW_STOCK_THRESHOLD && p.getActive())
                .count();

            if (lowStockCount > 0) {
                return Health.down()
                    .withDetail("lowStockProducts", lowStockCount)
                    .withDetail("message", "Produits en rupture imminente (stock < 5)")
                    .build();
            }

            long totalProducts = productRepository.findAll().size();
            return Health.up()
                .withDetail("totalProducts", totalProducts)
                .withDetail("message", "Tous les produits ont un stock suffisant")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Erreur lors de la vérification du stock")
                .withException(e)
                .build();
        }
    }
}
