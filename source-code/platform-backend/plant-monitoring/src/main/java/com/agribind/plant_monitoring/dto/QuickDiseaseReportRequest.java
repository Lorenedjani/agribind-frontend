package com.agribind.plant_monitoring.dto;

import org.springframework.web.multipart.MultipartFile;

public class QuickDiseaseReportRequest {
    private MultipartFile photo;
    private String location;
    private String crop;
    private String disease;
    private String severity;
    private String affectedArea;
    private String notes;
    private Double latitude;
    private Double longitude;
    
    // Getters and Setters
    public MultipartFile getPhoto() {
        return photo;
    }
    
    public void setPhoto(MultipartFile photo) {
        this.photo = photo;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getCrop() {
        return crop;
    }
    
    public void setCrop(String crop) {
        this.crop = crop;
    }
    
    public String getDisease() {
        return disease;
    }
    
    public void setDisease(String disease) {
        this.disease = disease;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getAffectedArea() {
        return affectedArea;
    }
    
    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
