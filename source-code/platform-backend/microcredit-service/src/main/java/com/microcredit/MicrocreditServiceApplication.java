package com.microcredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MicrocreditServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MicrocreditServiceApplication.class, args);
	}
}