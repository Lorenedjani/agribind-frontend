package com.agribind.location_service.service_impl;

import com.agribind.location_service.dto.GeocodingResponse;
import com.agribind.location_service.service.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Optional;

@Service
public class GeocodingServiceImpl implements GeocodingService {

    @Value("${geocoding.service.url:https://nominatim.openstreetmap.org}")
    private String geocodingServiceUrl;

    @Value("${geocoding.service.timeout:5000}")
    private int timeout;

    @Value("${geocoding.service.user-agent:CooperativeApp/1.0}")
    private String userAgent;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Optional<GeocodingResponse> geocodeAddress(String address) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(geocodingServiceUrl + "/search")
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("countrycodes", "cm") // Cameroon
                    .queryParam("addressdetails", 1);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GeocodingResponse[]> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    GeocodingResponse[].class
            );

            if (response.getBody() != null && response.getBody().length > 0) {
                return Optional.of(response.getBody()[0]);
            }

            return Optional.empty();
        } catch (Exception e) {
            // Log the error
            System.err.println("Geocoding error for address: " + address + " - " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<GeocodingResponse> reverseGeocode(Double latitude, Double longitude) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(geocodingServiceUrl + "/reverse")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("format", "json")
                    .queryParam("zoom", 10) // Region level
                    .queryParam("addressdetails", 1);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GeocodingResponse> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    GeocodingResponse.class
            );

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            // Log the error
            System.err.println("Reverse geocoding error for coordinates: " +
                             latitude + "," + longitude + " - " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String getRegionFromCoordinates(Double latitude, Double longitude) {
        Optional<GeocodingResponse> response = reverseGeocode(latitude, longitude);
        return response.map(GeocodingResponse::getRegionName)
                      .orElse("Unknown Region");
    }
}
