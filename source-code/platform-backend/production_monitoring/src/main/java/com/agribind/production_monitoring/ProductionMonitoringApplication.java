package com.agribind.production_monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient   // ← registers this service with Eureka
public class ProductionMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductionMonitoringApplication.class, args);
	}
}