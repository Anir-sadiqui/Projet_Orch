package com.membership.order.infrastructure.metrics;

import com.membership.order.domain.entity.OrderStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


@Component
public class OrderMetrics {

    private final Map<OrderStatus, Counter> ordersByStatus;
    private final AtomicReference<BigDecimal> dailyRevenue;

    public OrderMetrics(MeterRegistry meterRegistry) {

        this.ordersByStatus = new EnumMap<>(OrderStatus.class);

        for (OrderStatus status : OrderStatus.values()) {
            Counter counter = Counter.builder("orders.count")
                    .description("Nombre de commandes par statut")
                    .tag("status", status.name())
                    .register(meterRegistry);

            ordersByStatus.put(status, counter);
        }

        this.dailyRevenue = new AtomicReference<>(BigDecimal.ZERO);

        Gauge.builder(
                        "orders.daily.revenue",
                        dailyRevenue,
                        value -> value.get().doubleValue()
                )
                .description("Montant total des commandes du jour")
                .baseUnit("euros")
                .register(meterRegistry);
    }



    public void incrementStatus(OrderStatus status) {
        Counter counter = ordersByStatus.get(status);
        if (counter != null) {
            counter.increment();
        }
    }

    public void addRevenue(BigDecimal amount) {
        if (amount != null) {
            dailyRevenue.updateAndGet(current -> current.add(amount));
        }
    }


    public void resetDailyRevenue() {
        dailyRevenue.set(BigDecimal.ZERO);
    }
}
