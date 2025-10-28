package com.agribind.location_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeocodingResponse {

    @JsonProperty("lat")
    private String latitude;

    @JsonProperty("lon")
    private String longitude;

    @JsonProperty("display_name")
    private String displayName;

    private Address address;

    // Getters and Setters
    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public static class Address {
        private String state;
        private String county;
        private String city;
        private String village;
        private String municipality;

        // Getters and Setters
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getCounty() { return county; }
        public void setCounty(String county) { this.county = county; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getVillage() { return village; }
        public void setVillage(String village) { this.village = village; }

        public String getMunicipality() { return municipality; }
        public void setMunicipality(String municipality) { this.municipality = municipality; }
    }

    public Double getLatitudeAsDouble() {
        try {
            return latitude != null ? Double.parseDouble(latitude) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double getLongitudeAsDouble() {
        try {
            return longitude != null ? Double.parseDouble(longitude) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getRegionName() {
        if (address != null) {
            if (address.getState() != null) return address.getState();
            if (address.getCounty() != null) return address.getCounty();
            if (address.getCity() != null) return address.getCity();
            if (address.getMunicipality() != null) return address.getMunicipality();
            if (address.getVillage() != null) return address.getVillage();
        }
        return displayName;
    }
}
