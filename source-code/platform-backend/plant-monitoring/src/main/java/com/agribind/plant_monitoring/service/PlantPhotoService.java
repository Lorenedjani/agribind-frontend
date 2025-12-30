package com.agribind.plant_monitoring.service;

import com.agribind.plant_monitoring.dto.PlantPhotoUploadDTO;
import com.agribind.plant_monitoring.exception.FileStorageException;
import com.agribind.plant_monitoring.exception.ResourceNotFoundException;
import com.agribind.plant_monitoring.model.Plant;
import com.agribind.plant_monitoring.model.PlantPhoto;
import com.agribind.plant_monitoring.repository.PlantPhotoRepository;
import com.agribind.plant_monitoring.repository.PlantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PlantPhotoService {
    
    @Autowired
    private PlantPhotoRepository plantPhotoRepository;
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private PlantHealthAnalysisService healthAnalysisService;
    
    @Transactional
    public PlantPhoto uploadPhoto(PlantPhotoUploadDTO uploadDTO) {
        log.info("Uploading photo for plant ID: {}", uploadDTO.getPlantId());
        
        try {
            // 1. Validate plant exists
            Plant plant = plantRepository.findById(uploadDTO.getPlantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Plant not found with id: " + uploadDTO.getPlantId()));
            
            // 2. Extract image metadata
            FileStorageService.ImageMetadata metadata = 
                fileStorageService.extractImageMetadata(uploadDTO.getFile());
            
            // 3. Store the file
            String filename = fileStorageService.storeFile(uploadDTO.getFile());
            
            // 4. Create thumbnail
            String thumbnailFilename = fileStorageService.createThumbnail(filename);
            
            // 5. Create PlantPhoto entity
            PlantPhoto photo = new PlantPhoto();
            photo.setFilename(filename);
            photo.setOriginalFilename(uploadDTO.getFile().getOriginalFilename());
            photo.setFilePath(fileStorageService.loadFile(filename).toString());
            photo.setThumbnailPath(fileStorageService.loadFile(thumbnailFilename).toString());
            photo.setFileSize(metadata.getSize());
            photo.setContentType(metadata.getContentType());
            photo.setImageWidth(metadata.getWidth());
            photo.setImageHeight(metadata.getHeight());
            photo.setCaption(uploadDTO.getCaption());
            photo.setTakenAt(uploadDTO.getTakenAt() != null ? 
                uploadDTO.getTakenAt() : LocalDateTime.now());
            photo.setPlant(plant);
            photo.setUploadedAt(LocalDateTime.now());
            
            // 6. Save to database
            PlantPhoto savedPhoto = plantPhotoRepository.save(photo);
            log.info("Photo saved with ID: {}", savedPhoto.getId());
            
            // 7. Trigger async health analysis
            triggerHealthAnalysis(savedPhoto);
            
            return savedPhoto;
            
        } catch (IOException e) {
            log.error("Failed to upload photo: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to upload photo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading photo: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload photo: " + e.getMessage(), e);
        }
    }
    
    @Async
    public void triggerHealthAnalysis(PlantPhoto photo) {
        try {
            log.info("Starting health analysis for photo ID: {}", photo.getId());
            healthAnalysisService.analyzePlantHealth(photo)
                    .thenAccept(analysis -> {
                        log.info("Health analysis completed for photo ID: {}", photo.getId());
                    })
                    .exceptionally(ex -> {
                        log.error("Failed to analyze plant health for photo ID: {}", 
                                photo.getId(), ex);
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error triggering health analysis: {}", e.getMessage(), e);
        }
    }
    
    public PlantPhoto getPhotoById(Long photoId) {
        return plantPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plant photo not found with id: " + photoId));
    }
    
    public List<PlantPhoto> getPhotosByPlantId(Long plantId) {
        return plantPhotoRepository.findByPlantIdOrderByTakenAtDesc(plantId);
    }
    
    public List<PlantPhoto> getPhotosByPlantId(Long plantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "takenAt"));
        Page<PlantPhoto> photoPage = plantPhotoRepository.findAll(
            (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("plant").get("id"), plantId),
            pageable
        );
        return photoPage.getContent();
    }
    
    public List<PlantPhoto> getPhotosByPlantIdAndDate(Long plantId, LocalDateTime date) {
        return plantPhotoRepository.findByPlantIdAndDate(plantId, date);
    }
    
    public PlantPhoto getLatestPhotoByPlantId(Long plantId) {
        return plantPhotoRepository.findFirstByPlantIdOrderByTakenAtDesc(plantId);
    }
    
    @Transactional
    public void deletePhoto(Long photoId) {
        try {
            PlantPhoto photo = getPhotoById(photoId);
            
            // Delete file from storage
            fileStorageService.deleteFile(photo.getFilename());
            
            // Delete from database
            plantPhotoRepository.delete(photo);
            
            log.info("Photo deleted successfully: {}", photoId);
        } catch (IOException e) {
            log.error("Failed to delete photo file: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to delete photo file: " + e.getMessage(), e);
        }
    }
    
    public List<PlantPhoto> getUnprocessedPhotos() {
        // Get photos without health analysis
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> photo.getHealthAnalysis() == null)
                .limit(50) // Limit to 50 at a time
                .toList();
    }
    
    public List<PlantPhoto> getPhotosWithPoorHealth(Double threshold) {
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> photo.getHealthAnalysis() != null)
                .filter(photo -> photo.getHealthAnalysis().getHealthScore() < threshold)
                .toList();
    }
    
    public Long countPhotosByPlantId(Long plantId) {
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> photo.getPlant().getId().equals(plantId))
                .count();
    }
    
    public List<PlantPhoto> searchPhotos(String keyword) {
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> 
                    (photo.getCaption() != null && 
                     photo.getCaption().toLowerCase().contains(keyword.toLowerCase())) ||
                    (photo.getPlant().getName() != null && 
                     photo.getPlant().getName().toLowerCase().contains(keyword.toLowerCase()))
                )
                .toList();
    }
    
    @Transactional
    public PlantPhoto updatePhotoCaption(Long photoId, String newCaption) {
        PlantPhoto photo = getPhotoById(photoId);
        photo.setCaption(newCaption);
        return plantPhotoRepository.save(photo);
    }
    
    public byte[] getPhotoImage(Long photoId) throws IOException {
        PlantPhoto photo = getPhotoById(photoId);
        return fileStorageService.getFileAsBytes(photo.getFilename());
    }
    
    public byte[] getPhotoThumbnail(Long photoId) throws IOException {
        PlantPhoto photo = getPhotoById(photoId);
        if (photo.getThumbnailPath() == null) {
            throw new ResourceNotFoundException("Thumbnail not found for photo: " + photoId);
        }
        
        String thumbnailFilename = photo.getThumbnailPath()
                .substring(photo.getThumbnailPath().lastIndexOf("/") + 1);
        return fileStorageService.getFileAsBytes(thumbnailFilename);
    }
    
    public List<PlantPhoto> getRecentPhotos(int count) {
        Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        return plantPhotoRepository.findAll(pageable).getContent();
    }
    
    public List<PlantPhoto> getPhotosByHealthStatus(String healthStatus) {
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> photo.getHealthAnalysis() != null)
                .filter(photo -> photo.getHealthAnalysis().getHealthStatus().name()
                        .equalsIgnoreCase(healthStatus))
                .toList();
    }
    
    @Transactional
    public PlantPhoto reprocessHealthAnalysis(Long photoId) {
        PlantPhoto photo = getPhotoById(photoId);
        
        // Delete existing analysis if exists
        if (photo.getHealthAnalysis() != null) {
            // You would need a healthAnalysisRepository to delete it
            // For now, we'll just trigger new analysis
            photo.setHealthAnalysis(null);
            plantPhotoRepository.save(photo);
        }
        
        // Trigger new analysis
        triggerHealthAnalysis(photo);
        
        return photo;
    }
    
    public Double getAverageHealthScoreByPlant(Long plantId) {
        List<PlantPhoto> photos = getPhotosByPlantId(plantId);
        
        return photos.stream()
                .filter(photo -> photo.getHealthAnalysis() != null)
                .mapToDouble(photo -> photo.getHealthAnalysis().getHealthScore())
                .average()
                .orElse(0.0);
    }
    
    public List<PlantPhoto> getPhotosWithDisease() {
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> photo.getHealthAnalysis() != null)
                .filter(photo -> photo.getHealthAnalysis().getDiseaseDetected() != null)
                .toList();
    }
    
    @Transactional
    public void batchDeletePhotos(List<Long> photoIds) {
        photoIds.forEach(this::deletePhoto);
        log.info("Batch deleted {} photos", photoIds.size());
    }
    
    public List<PlantPhoto> getPhotosByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> !photo.getTakenAt().isBefore(startDate))
                .filter(photo -> !photo.getTakenAt().isAfter(endDate))
                .toList();
    }
}