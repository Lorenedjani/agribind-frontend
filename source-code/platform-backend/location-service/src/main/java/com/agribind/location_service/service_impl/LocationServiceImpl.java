package com.agribind.location_service.service_impl;

import com.agribind.location_service.dto.LocationRequest;
import com.agribind.location_service.dto.LocationResponse;
import com.agribind.location_service.model.Location;
import com.agribind.location_service.repository.LocationRepository;
import com.agribind.location_service.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Override
    public LocationResponse createLocation(LocationRequest request) {
        Location location = new Location();
        location.setRegion(request.getRegion());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setDescription(request.getDescription());

        Location savedLocation = locationRepository.save(location);
        return convertToResponse(savedLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations() {
        return locationRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocationResponse> getLocationById(Long id) {
        return locationRepository.findById(id)
                .map(this::convertToResponse);
    }

    @Override
    public Optional<LocationResponse> updateLocation(Long id, LocationRequest request) {
        return locationRepository.findById(id)
                .map(existingLocation -> {
                    existingLocation.setRegion(request.getRegion());
                    existingLocation.setLatitude(request.getLatitude());
                    existingLocation.setLongitude(request.getLongitude());
                    existingLocation.setDescription(request.getDescription());

                    Location updatedLocation = locationRepository.save(existingLocation);
                    return convertToResponse(updatedLocation);
                });
    }

    @Override
    public boolean deleteLocation(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> searchLocations(String region) {
        return locationRepository.searchLocations(region)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationsInBoundingBox(Double minLat, Double maxLat,
                                                          Double minLng, Double maxLng) {
        return locationRepository.findByBoundingBox(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationsInRadius(Double latitude, Double longitude, Double radiusKm) {
        // Convert km to degrees (approximate: 1° ≈ 111 km)
        double radiusInDegrees = radiusKm / 111.0;
        return locationRepository.findByRadius(latitude, longitude, radiusInDegrees)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean locationExists(Long id) {
        return locationRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long getLocationsCount() {
        return locationRepository.count();
    }

    private LocationResponse convertToResponse(Location location) {
        return new LocationResponse(
            location.getId(),
            location.getRegion(),
            location.getLatitude(),
            location.getLongitude(),
            location.getDescription(),
            location.getCreatedAt(),
            location.getUpdatedAt()
        );
    }
}
