package cm.agribind.usermanagement.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleMapsService {

    private final RestTemplate restTemplate;

    @Value("${agribind.google-maps.api-key}")
    private String apiKey;

    @Value("${agribind.google-maps.enabled:false}")
    private boolean enabled;

    public GeocodingResult geocodeAddress(String address) {
        if (!enabled) {
            log.warn("Google Maps API is disabled");
            return null;
        }

        try {
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    address.replace(" ", "+"),
                    apiKey
            );

            GoogleMapsResponse response = restTemplate.getForObject(url, GoogleMapsResponse.class);

            if (response != null && "OK".equals(response.getStatus()) &&
                    response.getResults() != null && !response.getResults().isEmpty()) {

                GoogleMapsResponse.Result result = response.getResults().get(0);
                GoogleMapsResponse.Location location = result.getGeometry().getLocation();

                return new GeocodingResult(
                        location.getLat(),
                        location.getLng(),
                        result.getFormattedAddress()
                );
            }
        } catch (Exception e) {
            log.error("Failed to geocode address: {}", address, e);
        }

        return null;
    }

    public String formatGpsCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return String.format("%.6f,%.6f", latitude, longitude);
    }

    @Data
    public static class GeocodingResult {
        private final Double latitude;
        private final Double longitude;
        private final String formattedAddress;
    }

    @Data
    private static class GoogleMapsResponse {
        private String status;
        private java.util.List<Result> results;

        @Data
        private static class Result {
            private String formattedAddress;
            private Geometry geometry;
        }

        @Data
        private static class Geometry {
            private Location location;
        }

        @Data
        private static class Location {
            private Double lat;
            private Double lng;
        }
    }
}