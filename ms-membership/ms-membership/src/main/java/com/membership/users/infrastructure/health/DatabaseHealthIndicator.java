package com.membership.users.infrastructure.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.membership.users.domain.repository.UserRepository;

/**
 * Health Indicator personnalisé pour vérifier l'état de la base de données.
 * Best practices :
 * - Implémente HealthIndicator pour les checks personnalisés
 * - Fournit des détails utiles pour le debugging
 * - Gère les exceptions proprement
 * - Utilisé par /actuator/health
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final UserRepository userRepository;

    public DatabaseHealthIndicator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Health health() {
        try {
            // Vérifie la connexion à la base de données
            long userCount = userRepository.count();
            long activeUserCount = userRepository.countActiveUsers();
            
            log.debug("Health check database - Total users: {}, Active users: {}", 
                    userCount, activeUserCount);
            
            return Health.up()
                    .withDetail("database", "H2")
                    .withDetail("status", "Connection OK")
                    .withDetail("totalUsers", userCount)
                    .withDetail("activeUsers", activeUserCount)
                    .build();
                    
        } catch (Exception e) {
            log.error("Health check database failed", e);
            
            return Health.down()
                    .withDetail("database", "H2")
                    .withDetail("status", "Connection Failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
