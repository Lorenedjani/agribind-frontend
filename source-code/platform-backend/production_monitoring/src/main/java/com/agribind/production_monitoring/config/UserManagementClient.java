package com.agribind.production_monitoring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP client to call user-management-service for farmer details (region, name).
 * Uses RestTemplate with the service URL from config.
 * Falls back gracefully if user-management-service is unreachable.
 */
@Slf4j
@Component
public class UserManagementClient {

    private final RestTemplate restTemplate;
    private final String userManagementBaseUrl;

    public UserManagementClient(
            RestTemplate restTemplate,
            @Value("${app.services.user-management-url:http://localhost:8083}") String userManagementBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.userManagementBaseUrl = userManagementBaseUrl;
    }

    /**
     * Fetch farmer region from user-management-service.
     * Returns null if the service is unreachable or user not found.
     */
    public String getFarmerRegion(String farmerId) {
        try {
            String url = userManagementBaseUrl + "/api/v1/users/" + farmerId;
            @SuppressWarnings("unchecked")
            Map<String, Object> user = restTemplate.getForObject(url, Map.class);
            if (user != null && user.get("region") != null) {
                return user.get("region").toString();
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Farmer {} not found in user-management-service", farmerId);
        } catch (Exception e) {
            log.warn("Could not fetch region for farmer {} from user-management-service: {}", farmerId, e.getMessage());
        }
        return null; // Return null on failure -> defaults to "Unknown" when saved
    }

    /**
     * Fetch farmer name from user-management-service.
     * Returns null if the service is unreachable or user not found.
     */
    public String getFarmerName(String farmerId) {
        try {
            String url = userManagementBaseUrl + "/api/v1/users/" + farmerId;
            @SuppressWarnings("unchecked")
            Map<String, Object> user = restTemplate.getForObject(url, Map.class);
            if (user != null && user.get("name") != null) {
                return user.get("name").toString();
            }
        } catch (Exception e) {
            log.warn("Could not fetch name for farmer {} from user-management-service: {}", farmerId, e.getMessage());
        }
        return null;
    }

    /**
     * Fetch both region and name in one call.
     * Returns a FarmerInfo record, fields may be null if unavailable.
     */
    public FarmerInfo getFarmerInfo(String farmerId) {
        try {
            String url = userManagementBaseUrl + "/api/v1/users/" + farmerId;
            @SuppressWarnings("unchecked")
            Map<String, Object> user = restTemplate.getForObject(url, Map.class);
            if (user != null) {
                String region = user.get("region") != null ? user.get("region").toString() : null;
                String name   = user.get("name")   != null ? user.get("name").toString()   : null;
                return new FarmerInfo(region, name);
            }
        } catch (Exception e) {
            log.warn("Could not fetch info for farmer {} from user-management-service: {}", farmerId, e.getMessage());
        }
        return new FarmerInfo(null, null);
    }

    public record FarmerInfo(String region, String name) {}
}

@Configuration
class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }
}
