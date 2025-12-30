package com.agribind.plant_monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/database")
    public Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test connection
            String version = jdbcTemplate.queryForObject(
                "SELECT version()", String.class
            );
            
            // Check connection count (Supabase specific)
            Integer connections = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM pg_stat_activity WHERE datname = current_database()", 
                Integer.class
            );
            
            health.put("status", "UP");
            health.put("database", "Supabase PostgreSQL");
            health.put("version", version);
            health.put("active_connections", connections);
            health.put("timestamp", java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }
}
