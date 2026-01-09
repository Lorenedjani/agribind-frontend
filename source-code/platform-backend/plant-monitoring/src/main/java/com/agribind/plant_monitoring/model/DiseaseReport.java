package com.agribind.plant_monitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disease_reports")
public class DiseaseReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "report_id", unique = true, nullable = false)
    private String reportId;
    
    @Column(name = "farmer_id", nullable = false)
    private Long farmerId;
    
    @Transient
    private String farmerName;
    
    @Column(name = "farmer_code")
    private String farmerCode;
    
    @Column(nullable = false)
    private String crop;
    
    @Column(nullable = false)
    private String disease;
    
    @Column(nullable = false)
    private String location;
    
    @Column(name = "affected_area")
    private String affectedArea;
    
    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity severity;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
    
    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;
    
    @Column(name = "reported_by")
    private String reportedBy;
    
    @Column(name = "treatment_notes", length = 2000)
    private String treatmentNotes;
    
    @Column(name = "reported_via")
    @Enumerated(EnumType.STRING)
    private ReportSource reportedVia;
    
    @Column(name = "photo_path")
    private String photoPath;
    
    @Column(name = "coordinates")
    private String coordinates;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.reportId == null) {
            this.reportId = generateReportId();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status == ReportStatus.RESOLVED && this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
    
    private String generateReportId() {
        return "DIS-" + String.format("%03d", (int)(Math.random() * 1000));
    }
    
    // Enum definitions
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }
    
    public enum ReportStatus {
        INVESTIGATING, UNDER_TREATMENT, MONITORING, RESOLVED
    }
    
    public enum ReportSource {
        QUICK_REPORT, MANUAL_ENTRY, AI_DETECTION, FARMER_REPORT
    }
    
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
    
    public String getFarmerCode() {
        return farmerCode;
    }
    
    public void setFarmerCode(String farmerCode) {
        this.farmerCode = farmerCode;
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
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public ReportStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReportStatus status) {
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
    
    public ReportSource getReportedVia() {
        return reportedVia;
    }
    
    public void setReportedVia(ReportSource reportedVia) {
        this.reportedVia = reportedVia;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    public String getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}