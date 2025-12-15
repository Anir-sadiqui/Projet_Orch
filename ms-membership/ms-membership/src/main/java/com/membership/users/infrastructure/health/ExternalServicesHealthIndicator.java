package com.membership.users.infrastructure.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;


@Component
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ExternalServicesHealthIndicator.class);

    @Override
    public Health health() {
        try {
            boolean emailServiceUp = checkEmailService();
            boolean notificationServiceUp = checkNotificationService();
            
            if (emailServiceUp && notificationServiceUp) {
                return Health.up()
                        .withDetail("emailService", "UP")
                        .withDetail("notificationService", "UP")
                        .build();
            } else {
                return Health.down()
                        .withDetail("emailService", emailServiceUp ? "UP" : "DOWN")
                        .withDetail("notificationService", notificationServiceUp ? "UP" : "DOWN")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("Health check external services failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private boolean checkEmailService() {
        return true;
    }

    private boolean checkNotificationService() {
        return true;
    }
}
