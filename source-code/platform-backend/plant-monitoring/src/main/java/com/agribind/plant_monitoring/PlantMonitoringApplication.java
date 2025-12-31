package com.agribind.plant_monitoring;

import com.agribind.plant_monitoring.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
@EnableAsync
@EnableScheduling
public class PlantMonitoringApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlantMonitoringApplication.class, args);
    }
}