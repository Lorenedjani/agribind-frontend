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

    public PlantPhotoUploadDTO() {
    }

    public PlantPhotoUploadDTO(Long plantId, MultipartFile file, String caption, LocalDateTime takenAt) {
        this.plantId = plantId;
        this.file = file;
        this.caption = caption;
        this.takenAt = takenAt;
    }

    public Long getPlantId() {
        return plantId;
    }

    public void setPlantId(Long plantId) {
        this.plantId = plantId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

}