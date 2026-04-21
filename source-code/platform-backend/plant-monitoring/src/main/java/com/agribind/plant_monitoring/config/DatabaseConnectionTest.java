package com.agribind.plant_monitoring.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionTest implements CommandLineRunner {
    
    private final JdbcTemplate jdbcTemplate;
    
    public DatabaseConnectionTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("DatabaseConnectionTest initialized");
    }
    
    @Override
    public void run(String... args) throws Exception {
        try {
            String result = jdbcTemplate.queryForObject("SELECT version()", String.class);
            System.out.println("✅ Database connected successfully!");
            System.out.println("Database version: " + result);
            
            // Test Supabase specific features
            jdbcTemplate.execute("SELECT 1");
            System.out.println("✅ Supabase connection established!");
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}