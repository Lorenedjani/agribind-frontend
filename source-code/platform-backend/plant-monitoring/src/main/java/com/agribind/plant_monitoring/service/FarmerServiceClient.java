package com.agribind.plant_monitoring.service;

import com.agribind.plant_monitoring.dto.FarmerDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.List;

@Service
public class FarmerServiceClient {
    
    @Value("${farmer.service.url}")
    private String farmerServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public FarmerServiceClient() {
        this.restTemplate = new RestTemplate();
    }
    
    public List<FarmerDTO> getFarmersByLocation(String location) {
        try {
            String url = farmerServiceUrl + "/api/farmers/search/by-location?location=" + location;
            ResponseEntity<FarmerDTO[]> response = restTemplate.getForEntity(url, FarmerDTO[].class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error fetching farmers by location: " + e.getMessage());
        }
        
        return List.of();
    }
    
    public FarmerDTO getFarmerById(Long farmerId) {
        try {
            String url = farmerServiceUrl + "/api/farmers/" + farmerId;
            ResponseEntity<FarmerDTO> response = restTemplate.getForEntity(url, FarmerDTO.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.err.println("Error fetching farmer by id: " + e.getMessage());
        }
        
        return null;
    }
}
