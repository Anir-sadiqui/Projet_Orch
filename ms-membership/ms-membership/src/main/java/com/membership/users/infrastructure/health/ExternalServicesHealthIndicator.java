package com.membership.users.infrastructure.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health Indicator personnalisé pour vérifier les services externes.
 * Best practice : Permet de monitorer la disponibilité des dépendances externes
 */
@Component
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ExternalServicesHealthIndicator.class);

    @Override
    public Health health() {
        try {
            // Simulation d'un check de service externe
            // Dans un cas réel, vous feriez un appel HTTP, une connexion à un service, etc.
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
        // Simulation - retourne toujours true
        // Dans un cas réel, vérifier la connexion au serveur SMTP
        return true;
    }

    private boolean checkNotificationService() {
        // Simulation - retourne toujours true
        // Dans un cas réel, faire un ping vers le service de notifications
        return true;
    }
}
