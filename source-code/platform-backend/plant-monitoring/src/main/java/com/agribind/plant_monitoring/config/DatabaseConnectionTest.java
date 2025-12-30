package com.agribind.plant_monitoring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DatabaseConnectionTest implements CommandLineRunner {
    
    private final JdbcTemplate jdbcTemplate;
    
    public DatabaseConnectionTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public void run(String... args) throws Exception {
        try {
            String result = jdbcTemplate.queryForObject("SELECT version()", String.class);
            log.info("✅ Database connected successfully!");
            log.info("Database version: {}", result);
            
            // Test Supabase specific features
            jdbcTemplate.execute("SELECT current_database()");
            log.info("✅ Supabase connection established!");
        } catch (Exception e) {
            log.error("❌ Failed to connect to database: {}", e.getMessage());
            throw e;
        }
    }
}