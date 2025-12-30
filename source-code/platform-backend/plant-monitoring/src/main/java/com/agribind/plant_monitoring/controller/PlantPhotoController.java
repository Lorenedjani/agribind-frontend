package com.agribind.plant_monitoring.controller;

import com.agribind.plant_monitoring.dto.PlantPhotoResponseDTO;
import com.agribind.plant_monitoring.dto.PlantPhotoUploadDTO;
import com.agribind.plant_monitoring.model.HealthAnalysis;
import com.agribind.plant_monitoring.model.Plant;
import com.agribins.plant_monitoring.model.PlantPhoto;
import com.agribind.plant_monitoring.service.FileStorageService;
import com.agribind.plant_monitoring.service.PlantHealthAnalysisService;
import com.agribind.plant_monitoring.service.PlantPhotoService;
import com.agribind.plant_monitoring.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant Photo Management", description = "Endpoints for uploading and managing plant photos")
@Slf4j
public class PlantPhotoController {
    
    @Autowired
    private PlantPhotoService plantPhotoService;
    
    @Autowired
    private PlantService plantService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private PlantHealthAnalysisService healthAnalysisService;
    
    @PostMapping("/{plantId}/photos/upload")
    @Operation(summary = "Upload a plant photo")
    public ResponseEntity<PlantPhotoResponseDTO> uploadPlantPhoto(
            @PathVariable Long plantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "takenAt", required = false) LocalDateTime takenAt) {
        
        log.info("Uploading photo for plant id: {}", plantId);
        
        PlantPhotoUploadDTO uploadDTO = new PlantPhotoUploadDTO();
        uploadDTO.setPlantId(plantId);
        uploadDTO.setFile(file);
        uploadDTO.setCaption(caption);
        uploadDTO.setTakenAt(takenAt != null ? takenAt : LocalDateTime.now());
        
        PlantPhoto photo = plantPhotoService.uploadPhoto(uploadDTO);
        
        // Start health analysis asynchronously
        healthAnalysisService.analyzePlantHealth(photo)
                .thenAccept(analysis -> {
                    log.info("Health analysis completed for photo: {}", photo.getId());
                })
                .exceptionally(ex -> {
                    log.error("Failed to analyze plant health for photo: {}", photo.getId(), ex);
                    return null;
                });
        
        PlantPhotoResponseDTO responseDTO = convertToDTO(photo);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(photo.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(responseDTO);
    }
    
    @GetMapping("/{plantId}/photos")
    @Operation(summary = "Get all photos for a plant")
    public ResponseEntity<List<PlantPhotoResponseDTO>> getPlantPhotos(
            @PathVariable Long plantId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        List<PlantPhoto> photos = plantPhotoService.getPhotosByPlantId(plantId, page, size);
        List<PlantPhotoResponseDTO> response = photos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/photos/{photoId}")
    @Operation(summary = "Get a specific plant photo")
    public ResponseEntity<PlantPhotoResponseDTO> getPlantPhoto(@PathVariable Long photoId) {
        PlantPhoto photo = plantPhotoService.getPhotoById(photoId);
        return ResponseEntity.ok(convertToDTO(photo));
    }
    
    @GetMapping("/photos/{photoId}/health-analysis")
    @Operation(summary = "Get health analysis for a photo")
    public ResponseEntity<?> getPhotoHealthAnalysis(@PathVariable Long photoId) {
        PlantPhoto photo = plantPhotoService.getPhotoById(photoId);
        
        if (photo.getHealthAnalysis() == null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Health analysis in progress...");
        }
        
        return ResponseEntity.ok(healthAnalysisService.convertToDTO(photo.getHealthAnalysis()));
    }
    
    @DeleteMapping("/photos/{photoId}")
    @Operation(summary = "Delete a plant photo")
    public ResponseEntity<Void> deletePlantPhoto(@PathVariable Long photoId) {
        plantPhotoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{plantId}/health-summary")
    @Operation(summary = "Get plant health summary")
    public ResponseEntity<?> getPlantHealthSummary(@PathVariable Long plantId) {
        return ResponseEntity.ok(plantService.getPlantHealthSummary(plantId));
    }
    
    @GetMapping(value = "/photos/{photoId}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "Get plant photo image")
    public ResponseEntity<byte[]> getPlantPhotoImage(@PathVariable Long photoId) throws IOException {
        PlantPhoto photo = plantPhotoService.getPhotoById(photoId);
        byte[] imageBytes = fileStorageService.getFileAsBytes(photo.getFilename());
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .body(imageBytes);
    }
    
    @GetMapping(value = "/photos/{photoId}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "Get plant photo thumbnail")
    public ResponseEntity<byte[]> getPlantPhotoThumbnail(@PathVariable Long photoId) throws IOException {
        PlantPhoto photo = plantPhotoService.getPhotoById(photoId);
        
        if (photo.getThumbnailPath() == null) {
            return ResponseEntity.notFound().build();
        }
        
        byte[] imageBytes = fileStorageService.getFileAsBytes(
            photo.getThumbnailPath().substring(photo.getThumbnailPath().lastIndexOf("/") + 1)
        );
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .body(imageBytes);
    }
    
    private PlantPhotoResponseDTO convertToDTO(PlantPhoto photo) {
        PlantPhotoResponseDTO dto = new PlantPhotoResponseDTO();
        dto.setId(photo.getId());
        dto.setFilename(photo.getFilename());
        dto.setOriginalFilename(photo.getOriginalFilename());
        dto.setFileSize(photo.getFileSize());
        dto.setContentType(photo.getContentType());
        dto.setImageWidth(photo.getImageWidth());
        dto.setImageHeight(photo.getImageHeight());
        dto.setCaption(photo.getCaption());
        dto.setTakenAt(photo.getTakenAt());
        dto.setUploadedAt(photo.getUploadedAt());
        dto.setPlantId(photo.getPlant().getId());
        dto.setPlantName(photo.getPlant().getName());
        
        // Generate URLs
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        dto.setFileUrl(baseUrl + "/uploads/" + photo.getFilename());
        dto.setThumbnailUrl(baseUrl + "/uploads/thumb_" + photo.getFilename());
        
        // Include health analysis if available
        if (photo.getHealthAnalysis() != null) {
            dto.setHealthAnalysis(healthAnalysisService.convertToDTO(photo.getHealthAnalysis()));
        }
        
        return dto;
    }
}