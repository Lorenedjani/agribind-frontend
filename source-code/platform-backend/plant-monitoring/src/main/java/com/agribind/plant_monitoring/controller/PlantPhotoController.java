package com.agribind.plant_monitoring.controller;

import com.agribind.plant_monitoring.dto.PlantPhotoResponseDTO;
import com.agribind.plant_monitoring.dto.PlantPhotoUploadDTO;
import com.agribind.plant_monitoring.model.PlantPhoto;
import com.agribind.plant_monitoring.service.FileStorageService;
import com.agribind.plant_monitoring.service.PlantHealthAnalysisService;
import com.agribind.plant_monitoring.service.PlantPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant Photo Management", description = "Endpoints for uploading and managing plant photos")
public class PlantPhotoController {
    
    private final PlantPhotoService plantPhotoService;
    private final FileStorageService fileStorageService;
    private final PlantHealthAnalysisService healthAnalysisService;
    
    @Autowired
    public PlantPhotoController(
            PlantPhotoService plantPhotoService,
            FileStorageService fileStorageService,
            PlantHealthAnalysisService healthAnalysisService) {
        this.plantPhotoService = plantPhotoService;
        this.fileStorageService = fileStorageService;
        this.healthAnalysisService = healthAnalysisService;
        System.out.println("PlantPhotoController initialized");
    }
    
    @PostMapping(value = "/{plantId}/photos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a plant photo", 
               description = "Upload a photo for plant health monitoring with optional caption and timestamp")
    public ResponseEntity<PlantPhotoResponseDTO> uploadPlantPhoto(
            @PathVariable Long plantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "takenAt", required = false) LocalDateTime takenAt) {
        
        System.out.println("Uploading photo for plant ID: " + plantId);
        
        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        PlantPhotoUploadDTO uploadDTO = new PlantPhotoUploadDTO();
        uploadDTO.setPlantId(plantId);
        uploadDTO.setFile(file);
        uploadDTO.setCaption(caption);
        uploadDTO.setTakenAt(takenAt != null ? takenAt : LocalDateTime.now());
        
        PlantPhoto photo = plantPhotoService.uploadPhoto(uploadDTO);
        PlantPhotoResponseDTO responseDTO = convertToDTO(photo);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(photo.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(responseDTO);
    }
    
    @PostMapping("/{plantId}/photos/batch-upload")
    @Operation(summary = "Upload multiple plant photos")
    public ResponseEntity<List<PlantPhotoResponseDTO>> uploadMultiplePhotos(
            @PathVariable Long plantId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "caption", required = false) String caption) {
        
        System.out.println("Uploading " + files.size() + " photos for plant ID: " + plantId);
        
        List<PlantPhotoResponseDTO> responses = files.stream()
                .map(file -> {
                    PlantPhotoUploadDTO uploadDTO = new PlantPhotoUploadDTO();
                    uploadDTO.setPlantId(plantId);
                    uploadDTO.setFile(file);
                    uploadDTO.setCaption(caption);
                    uploadDTO.setTakenAt(LocalDateTime.now());
                    
                    try {
                        PlantPhoto photo = plantPhotoService.uploadPhoto(uploadDTO);
                        return convertToDTO(photo);
                    } catch (Exception e) {
                        System.err.println("Failed to upload file: " + file.getOriginalFilename());
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    @GetMapping("/{plantId}/photos")
    @Operation(summary = "Get all photos for a plant")
    public ResponseEntity<List<PlantPhotoResponseDTO>> getPlantPhotos(
            @PathVariable Long plantId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "takenAt,desc") String sort) {
        
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
    
    @GetMapping("/photos/{photoId}/download")
    @Operation(summary = "Download plant photo")
    public ResponseEntity<Resource> downloadPlantPhoto(@PathVariable Long photoId) throws IOException {
        PlantPhoto photo = plantPhotoService.getPhotoById(photoId);
        byte[] fileContent = plantPhotoService.getPhotoImage(photoId);
        
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + photo.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .contentLength(fileContent.length)
                .body(resource);
    }
    
    @GetMapping(value = "/photos/{photoId}/image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @Operation(summary = "Get plant photo as image")
    public ResponseEntity<byte[]> getPlantPhotoImage(@PathVariable Long photoId) throws IOException {
        PlantPhoto photo = plantPhotoService.getPhotoById(photoId);
        byte[] imageBytes = plantPhotoService.getPhotoImage(photoId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .body(imageBytes);
    }
    
    @GetMapping(value = "/photos/{photoId}/thumbnail", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @Operation(summary = "Get plant photo thumbnail")
    public ResponseEntity<byte[]> getPlantPhotoThumbnail(@PathVariable Long photoId) throws IOException {
        PlantPhoto photo = plantPhotoService.getPhotoById(photoId);
        byte[] imageBytes = plantPhotoService.getPhotoThumbnail(photoId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .body(imageBytes);
    }
    
    @GetMapping("/photos/{photoId}/health-analysis")
    @Operation(summary = "Get health analysis for a photo")
    public ResponseEntity<?> getPhotoHealthAnalysis(@PathVariable Long photoId) {
        PlantPhoto photo = plantPhotoService.getPhotoById(photoId);
        
        if (photo.getHealthAnalysis() == null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(
                        "status", "processing",
                        "message", "Health analysis in progress...",
                        "photoId", photoId,
                        "estimatedTime", "30 seconds"
                    ));
        }
        
        return ResponseEntity.ok(healthAnalysisService.convertToDTO(photo.getHealthAnalysis()));
    }
    
    @PostMapping("/photos/{photoId}/reprocess")
    @Operation(summary = "Reprocess health analysis for a photo")
    public ResponseEntity<Map<String, Object>> reprocessHealthAnalysis(@PathVariable Long photoId) {
        PlantPhoto photo = plantPhotoService.reprocessHealthAnalysis(photoId);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Health analysis reprocessing started",
            "photoId", photoId,
            "estimatedCompletion", "30 seconds"
        ));
    }
    
    @GetMapping("/photos/search")
    @Operation(summary = "Search photos by keyword")
    public ResponseEntity<List<PlantPhotoResponseDTO>> searchPhotos(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        List<PlantPhoto> photos = plantPhotoService.searchPhotos(keyword);
        List<PlantPhotoResponseDTO> response = photos.stream()
                .skip(page * size)
                .limit(size)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/photos/health-status/{status}")
    @Operation(summary = "Get photos by health status")
    public ResponseEntity<List<PlantPhotoResponseDTO>> getPhotosByHealthStatus(
            @PathVariable String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        List<PlantPhoto> photos = plantPhotoService.getPhotosByHealthStatus(status);
        List<PlantPhotoResponseDTO> response = photos.stream()
                .skip(page * size)
                .limit(size)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/photos/diseases")
    @Operation(summary = "Get photos with detected diseases")
    public ResponseEntity<List<PlantPhotoResponseDTO>> getPhotosWithDiseases(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        List<PlantPhoto> photos = plantPhotoService.getPhotosWithDisease();
        List<PlantPhotoResponseDTO> response = photos.stream()
                .skip(page * size)
                .limit(size)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/photos/recent")
    @Operation(summary = "Get recent photos")
    public ResponseEntity<List<PlantPhotoResponseDTO>> getRecentPhotos(
            @RequestParam(defaultValue = "10") int count) {
        
        List<PlantPhoto> photos = plantPhotoService.getRecentPhotos(count);
        List<PlantPhotoResponseDTO> response = photos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{plantId}/photos/stats")
    @Operation(summary = "Get photo statistics for a plant")
    public ResponseEntity<Map<String, Object>> getPhotoStats(@PathVariable Long plantId) {
        Long totalPhotos = plantPhotoService.countPhotosByPlantId(plantId);
        Double avgHealthScore = plantPhotoService.getAverageHealthScoreByPlant(plantId);
        List<PlantPhoto> recentPhotos = plantPhotoService.getRecentPhotos(5);
        
        Map<String, Object> stats = Map.of(
            "plantId", plantId,
            "totalPhotos", totalPhotos,
            "averageHealthScore", String.format("%.2f", avgHealthScore),
            "recentPhotosCount", recentPhotos.size(),
            "lastUpload", recentPhotos.isEmpty() ? null : recentPhotos.get(0).getUploadedAt()
        );
        
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/photos/{photoId}/caption")
    @Operation(summary = "Update photo caption")
    public ResponseEntity<PlantPhotoResponseDTO> updatePhotoCaption(
            @PathVariable Long photoId,
            @RequestBody Map<String, String> request) {
        
        String newCaption = request.get("caption");
        PlantPhoto updatedPhoto = plantPhotoService.updatePhotoCaption(photoId, newCaption);
        
        return ResponseEntity.ok(convertToDTO(updatedPhoto));
    }
    
    @DeleteMapping("/photos/{photoId}")
    @Operation(summary = "Delete a plant photo")
    public ResponseEntity<Void> deletePlantPhoto(@PathVariable Long photoId) {
        plantPhotoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/photos/batch-delete")
    @Operation(summary = "Delete multiple photos")
    public ResponseEntity<Void> batchDeletePhotos(@RequestBody List<Long> photoIds) {
        plantPhotoService.batchDeletePhotos(photoIds);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/photos/date-range")
    @Operation(summary = "Get photos by date range")
    public ResponseEntity<List<PlantPhotoResponseDTO>> getPhotosByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        
        List<PlantPhoto> photos = plantPhotoService.getPhotosByDateRange(startDate, endDate);
        List<PlantPhotoResponseDTO> response = photos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
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
        dto.setFileUrl(baseUrl + "/api/v1/plants/photos/" + photo.getId() + "/image");
        dto.setThumbnailUrl(baseUrl + "/api/v1/plants/photos/" + photo.getId() + "/thumbnail");
        dto.setDownloadUrl(baseUrl + "/api/v1/plants/photos/" + photo.getId() + "/download");
        
        // Include health analysis if available
        if (photo.getHealthAnalysis() != null) {
            dto.setHealthAnalysis(healthAnalysisService.convertToDTO(photo.getHealthAnalysis()));
        }
        
        return dto;
    }
}