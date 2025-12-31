package com.agribind.plant_monitoring.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "plant_photos")
@Data
public class PlantPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(nullable = false)
    private String filename;
    
    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Column(name = "original_filename")
    private String originalFilename;
    
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    @Column(name = "file_path")
    private String filePath;
    
    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Column(name = "thumbnail_path")
    private String thumbnailPath;
    
    public String getThumbnailPath() {
        return this.thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    @Column(name = "file_size")
    private Long fileSize;
    
    public Long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Column(name = "content_type")
    private String contentType;
    
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Column(name = "image_width")
    private Integer imageWidth;
    
    public Integer getImageWidth() {
        return this.imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    @Column(name = "image_height")
    private Integer imageHeight;
    
    public Integer getImageHeight() {
        return this.imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    @Column(length = 500)
    private String caption;
    

    public String getCaption() {
        return this.caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Column(name = "taken_at")
    private LocalDateTime takenAt;
    
    public LocalDateTime getTakenAt() {
        return this.takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;
    
    public Plant getPlant() {
        return this.plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    @OneToOne(mappedBy = "photo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HealthAnalysis healthAnalysis;
    

    public HealthAnalysis getHealthAnalysis() {
        return this.healthAnalysis;
    }

    public void setHealthAnalysis(HealthAnalysis healthAnalysis) {
        this.healthAnalysis = healthAnalysis;
    }

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    public LocalDateTime getUploadedAt() {
        return this.uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
}