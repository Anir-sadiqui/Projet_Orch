package com.membership.product.infrastructure.metrics;

import com.membership.product.domain.entity.ProductCategory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ProductMetrics {

    private final MeterRegistry meterRegistry;

    public ProductMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementProductCreated(ProductCategory category) {
        Counter.builder("product.created.count")
                .description("Number of products created")
                .tag("category", category.name())
                .register(meterRegistry)
                .increment();
    }
}
