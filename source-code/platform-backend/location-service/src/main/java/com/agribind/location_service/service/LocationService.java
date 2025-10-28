package com.agribind.location_service.service;

import com.agribind.location_service.dto.LocationRequest;
import com.agribind.location_service.dto.LocationResponse;

import java.util.List;
import java.util.Optional;

public interface LocationService {

    LocationResponse createLocation(LocationRequest request);

    List<LocationResponse> getAllLocations();

    Optional<LocationResponse> getLocationById(Long id);

    Optional<LocationResponse> updateLocation(Long id, LocationRequest request);

    boolean deleteLocation(Long id);

    List<LocationResponse> searchLocations(String region);

    List<LocationResponse> getLocationsInBoundingBox(Double minLat, Double maxLat,
                                                   Double minLng, Double maxLng);

    List<LocationResponse> getLocationsInRadius(Double latitude, Double longitude, Double radiusKm);

    boolean locationExists(Long id);

    long getLocationsCount();
}