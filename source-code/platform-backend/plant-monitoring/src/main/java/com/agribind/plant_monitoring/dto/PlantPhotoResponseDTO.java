package com.agribind.plant_monitoring.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlantPhotoResponseDTO {
    private Long id;
    private String filename;
    private String originalFilename;
    private String fileUrl;
    private String thumbnailUrl;
    private String downloadUrl;  // Add this line
    private Long fileSize;
    private String contentType;
    private Integer imageWidth;
    private Integer imageHeight;
    private String caption;
    private LocalDateTime takenAt;
    private LocalDateTime uploadedAt;
    private Long plantId;
    private String plantName;
    private HealthAnalysisDTO healthAnalysis;

    public PlantPhotoResponseDTO() {
    }

    public PlantPhotoResponseDTO(Long id, String filename, String originalFilename, String fileUrl, String thumbnailUrl, String downloadUrl, Long fileSize, String contentType, Integer imageWidth, Integer imageHeight, String caption, LocalDateTime takenAt, LocalDateTime uploadedAt, Long plantId, String plantName) {
        this.id = id;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileUrl = fileUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.downloadUrl = downloadUrl;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.caption = caption;
        this.takenAt = takenAt;
        this.uploadedAt = uploadedAt;
        this.plantId = plantId;
        this.plantName = plantName;
    }

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getOriginalFilename() {
		return this.originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getFileUrl() {
		return this.fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getThumbnailUrl() {
		return this.thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getDownloadUrl() {
		return this.downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public Long getFileSize() {
		return this.fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Integer getImageWidth() {
		return this.imageWidth;
	}

	public void setImageWidth(Integer imageWidth) {
		this.imageWidth = imageWidth;
	}

	public Integer getImageHeight() {
		return this.imageHeight;
	}

	public void setImageHeight(Integer imageHeight) {
		this.imageHeight = imageHeight;
	}

	public String getCaption() {
		return this.caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public LocalDateTime getTakenAt() {
		return this.takenAt;
	}

	public void setTakenAt(LocalDateTime takenAt) {
		this.takenAt = takenAt;
	}

	public LocalDateTime getUploadedAt() {
		return this.uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public Long getPlantId() {
		return this.plantId;
	}

	public void setPlantId(Long plantId) {
		this.plantId = plantId;
	}

	public String getPlantName() {
		return this.plantName;
	}

	public void setPlantName(String plantName) {
		this.plantName = plantName;
	}

	public HealthAnalysisDTO getHealthAnalysis() {
		return this.healthAnalysis;
	}

	public void setHealthAnalysis(HealthAnalysisDTO healthAnalysis) {
		this.healthAnalysis = healthAnalysis;
	}

}