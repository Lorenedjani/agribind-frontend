package com.agribind.location_service.dto;

public class LocationResponse {
    private Long id;
    private String region;
    private Double latitude;
    private Double longitude;
    private String description;

    // Constructors
    public LocationResponse() {}

    public LocationResponse(Long id, String region, Double latitude, Double longitude, String description) {
        this.id = id;
        this.region = region;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
