package com.agribind.production_monitoring.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbInitializer {

    private final DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDatabase() {
        try {
            log.info("Initializing database stored procedures...");
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/analytics_procedures.sql"));
            populator.setSeparator("^^^"); // We'll add this to the SQL file to demarcate blocks
            populator.execute(dataSource);
            log.info("Database stored procedures initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize database stored procedures: {}", e.getMessage());
        }
    }
}
