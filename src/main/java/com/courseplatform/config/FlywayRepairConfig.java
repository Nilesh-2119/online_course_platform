package com.courseplatform.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true")
public class FlywayRepairConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayRepairConfig.class);

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                log.info("Executing Flyway repair to clear any failed migrations and align checksums...");
                flyway.repair();
                log.info("Executing Flyway migrate...");
                flyway.migrate();
                log.info("Flyway migration completed successfully.");
            } catch (Exception e) {
                log.warn("Flyway migration exception intercepted: {}. Continuing with Hibernate ddl-auto update.", e.getMessage());
            }
        };
    }
}
