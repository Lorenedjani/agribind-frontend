package com.agribind.location_service.service;

import com.agribind.location_service.dto.GeocodingResponse;

import java.util.Optional;

public interface GeocodingService {

    Optional<GeocodingResponse> geocodeAddress(String address);

    Optional<GeocodingResponse> reverseGeocode(Double latitude, Double longitude);

    String getRegionFromCoordinates(Double latitude, Double longitude);
}
