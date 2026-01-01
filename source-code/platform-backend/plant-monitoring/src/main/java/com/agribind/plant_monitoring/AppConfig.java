package com.agribind.plant_monitoring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class AppConfig {
    
    @Bean
    CommandLineRunner initStorageDirs() {
        return args -> {
            // Create disease reports upload directory
            Files.createDirectories(Paths.get("./uploads/disease-reports"));
            System.out.println("Disease reports upload directory created");
            
            // Create logs directory if it doesn't exist
            Files.createDirectories(Paths.get("./logs"));
            System.out.println("Logs directory created");
        };
    }
}