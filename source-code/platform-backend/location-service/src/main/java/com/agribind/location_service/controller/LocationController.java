package com.agribind.location_service.controller;

import com.agribind.location_service.dto.LocationRequest;
import com.agribind.location_service.dto.LocationResponse;
import com.agribind.location_service.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*") // Adjust for production
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationRequest request) {
        LocationResponse response = locationService.createLocation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        List<LocationResponse> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long id) {
        Optional<LocationResponse> location = locationService.getLocationById(id);
        return location.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationRequest request) {

        Optional<LocationResponse> updatedLocation = locationService.updateLocation(id, request);
        return updatedLocation.map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        boolean deleted = locationService.deleteLocation(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationResponse>> searchLocations(
            @RequestParam(required = false) String region) {

        List<LocationResponse> locations = locationService.searchLocations(region);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/bounding-box")
    public ResponseEntity<List<LocationResponse>> getLocationsInBoundingBox(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng) {

        List<LocationResponse> locations = locationService.getLocationsInBoundingBox(
            minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(locations);
    }
}