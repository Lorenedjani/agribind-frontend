package com.agribind.plant_monitoring.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class PlantPhotoUploadDTO {
    private Long plantId;
    private MultipartFile file;
    private String caption;
    private LocalDateTime takenAt;
}