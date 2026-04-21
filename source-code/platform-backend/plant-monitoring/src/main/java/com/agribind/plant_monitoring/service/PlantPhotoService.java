package com.agribind.plant_monitoring.service;

import com.agribind.plant_monitoring.dto.PlantPhotoUploadDTO;
import com.agribind.plant_monitoring.exception.FileStorageException;
import com.agribind.plant_monitoring.exception.ResourceNotFoundException;
import com.agribind.plant_monitoring.model.Plant;
import com.agribind.plant_monitoring.model.PlantPhoto;
import com.agribind.plant_monitoring.repository.PlantPhotoRepository;
import com.agribind.plant_monitoring.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlantPhotoService {
    
    private final PlantPhotoRepository plantPhotoRepository;
    private final PlantRepository plantRepository;
    private final FileStorageService fileStorageService;
    private final PlantHealthAnalysisService healthAnalysisService;
    
    @Autowired
    public PlantPhotoService(
            PlantPhotoRepository plantPhotoRepository,
            PlantRepository plantRepository,
            FileStorageService fileStorageService,
            PlantHealthAnalysisService healthAnalysisService) {
        this.plantPhotoRepository = plantPhotoRepository;
        this.plantRepository = plantRepository;
        this.fileStorageService = fileStorageService;
        this.healthAnalysisService = healthAnalysisService;
        System.out.println("PlantPhotoService initialized");
    }
    
    @Transactional
    public PlantPhoto uploadPhoto(PlantPhotoUploadDTO uploadDTO) {
        System.out.println("Uploading photo for plant ID: " + uploadDTO.getPlantId());
        
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
            System.out.println("Photo saved with ID: " + savedPhoto.getId());
            
            // 7. Trigger async health analysis
            triggerHealthAnalysis(savedPhoto);
            
            return savedPhoto;
            
        } catch (IOException e) {
            System.err.println("Failed to upload photo: " + e.getMessage());
            e.printStackTrace();
            throw new FileStorageException("Failed to upload photo: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error uploading photo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to upload photo: " + e.getMessage(), e);
        }
    }
    
    @Async
    public void triggerHealthAnalysis(PlantPhoto photo) {
        try {
            System.out.println("Starting health analysis for photo ID: " + photo.getId());
            healthAnalysisService.analyzePlantHealth(photo)
                    .thenAccept(analysis -> {
                        System.out.println("Health analysis completed for photo ID: " + photo.getId());
                    })
                    .exceptionally(ex -> {
                        System.err.println("Failed to analyze plant health for photo ID: " + photo.getId());
                        ex.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Error triggering health analysis: " + e.getMessage());
            e.printStackTrace();
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
        
        // Use Specification for filtering
        Page<PlantPhoto> photoPage = plantPhotoRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("plant").get("id"), plantId));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        
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
            
            System.out.println("Photo deleted successfully: " + photoId);
        } catch (IOException e) {
            System.err.println("Failed to delete photo file: " + e.getMessage());
            e.printStackTrace();
            throw new FileStorageException("Failed to delete photo file: " + e.getMessage(), e);
        }
    }
    
    public List<PlantPhoto> getUnprocessedPhotos() {
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> photo.getHealthAnalysis() == null)
                .limit(50)
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
        System.out.println("Batch deleted " + photoIds.size() + " photos");
    }
    
    public List<PlantPhoto> getPhotosByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return plantPhotoRepository.findAll().stream()
                .filter(photo -> !photo.getTakenAt().isBefore(startDate))
                .filter(photo -> !photo.getTakenAt().isAfter(endDate))
                .toList();
    }
}