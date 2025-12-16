package com.membership.product.infrastructure.health;

import com.membership.product.domain.repository.ProductRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("stockHealth")
public class StockHealthIndicator implements HealthIndicator {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private final ProductRepository repository;

    public StockHealthIndicator(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Health health() {
        long lowStockCount = repository.countByStockLessThan(LOW_STOCK_THRESHOLD);

        if (lowStockCount > 0) {
            return Health.status("DEGRADED")
                    .withDetail("lowStockProducts", lowStockCount)
                    .withDetail("threshold", LOW_STOCK_THRESHOLD)
                    .build();
        }

        return Health.up()
                .withDetail("message", "Stock levels are sufficient")
                .build();
    }
}
