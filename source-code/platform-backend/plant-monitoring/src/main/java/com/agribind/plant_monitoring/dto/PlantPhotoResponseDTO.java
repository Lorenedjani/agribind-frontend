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
}