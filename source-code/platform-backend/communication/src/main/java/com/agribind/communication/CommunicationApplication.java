package com.agribind.communication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class CommunicationApplication {

    private static final Logger log = LoggerFactory.getLogger(CommunicationApplication.class);

    private final DataSource dataSource;
    private final Environment env;

    public CommunicationApplication(DataSource dataSource, Environment env) {
        this.dataSource = dataSource;
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunicationApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=========================================");
        log.info("Communication Service Started Successfully");
        log.info("Profile: {}", env.getActiveProfiles());
        log.info("Database URL: {}", env.getProperty("spring.datasource.url"));

        // Verify database connection
        try (Connection connection = dataSource.getConnection()) {
            log.info("Database connection successful");
            log.info("Database: {}", connection.getMetaData().getDatabaseProductName());
            log.info("Version: {}", connection.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            log.error("Database connection failed: {}", e.getMessage());
        }

        log.info("=========================================");
    }
}