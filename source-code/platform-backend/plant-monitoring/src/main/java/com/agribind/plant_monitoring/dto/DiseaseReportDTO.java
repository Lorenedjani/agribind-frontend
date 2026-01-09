package com.agribind.plant_monitoring.dto;

import java.time.LocalDateTime;

public class DiseaseReportDTO {
    private Long id;
    private String reportId;
    private Long farmerId;
    private String farmerName;
    private String crop;
    private String disease;
    private String location;
    private String affectedArea;
    private String severity;
    private String status;
    private LocalDateTime reportDate;
    private String reportedBy;
    private String treatmentNotes;
    private String photoPath;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public Long getFarmerId() {
        return farmerId;
    }
    
    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }
    
    public String getFarmerName() {
        return farmerName;
    }
    
    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
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
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getAffectedArea() {
        return affectedArea;
    }
    
    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getReportDate() {
        return reportDate;
    }
    
    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
    }
    
    public String getReportedBy() {
        return reportedBy;
    }
    
    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }
    
    public String getTreatmentNotes() {
        return treatmentNotes;
    }
    
    public void setTreatmentNotes(String treatmentNotes) {
        this.treatmentNotes = treatmentNotes;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}