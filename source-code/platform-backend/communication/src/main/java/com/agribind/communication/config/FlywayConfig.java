// src/main/java/com/agribind/communication/config/FlywayConfig.java
package com.agribind.communication.config;

//import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    private final Environment env;

    public FlywayConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Custom migration strategy with retry logic
            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                try {
                    log.info("Attempting Flyway migration (attempt {}/{})", attempt + 1, maxRetries);
                    flyway.migrate();
                    log.info("Flyway migration completed successfully");
                    break;
                } catch (Exception e) {
                    attempt++;
                    if (attempt == maxRetries) {
                        log.error("Flyway migration failed after {} attempts", maxRetries, e);
                        throw e;
                    }
                    log.warn("Flyway migration attempt {} failed, retrying...", attempt);
                    try {
                        Thread.sleep(5000); // Wait 5 seconds before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .validateOnMigrate(true)
                .outOfOrder(false)
                .cleanDisabled(true)
                .schemas("public")
                .load();
    }
}