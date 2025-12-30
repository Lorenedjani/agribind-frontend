package com.agribind.plant_monitoring.repository;

import com.agribind.plant_monitoring.model.PlantPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlantPhotoRepository extends JpaRepository<PlantPhoto, Long> {
    
    List<PlantPhoto> findByPlantIdOrderByTakenAtDesc(Long plantId);
    
    @Query("SELECT p FROM PlantPhoto p WHERE p.plant.id = :plantId AND " +
           "DATE(p.takenAt) = DATE(:date) ORDER BY p.takenAt DESC")
    List<PlantPhoto> findByPlantIdAndDate(@Param("plantId") Long plantId, 
                                          @Param("date") LocalDateTime date);
    
    PlantPhoto findFirstByPlantIdOrderByTakenAtDesc(Long plantId);
    
    @Query("SELECT p FROM PlantPhoto p WHERE p.healthAnalysis IS NULL ORDER BY p.uploadedAt DESC")
    List<PlantPhoto> findUnprocessedPhotos();
    
    @Query("SELECT p FROM PlantPhoto p WHERE p.healthAnalysis IS NOT NULL " +
           "AND p.healthAnalysis.healthScore < :threshold ORDER BY p.healthAnalysis.healthScore ASC")
    List<PlantPhoto> findPhotosWithPoorHealth(@Param("threshold") Double threshold);
    
    @Query("SELECT COUNT(p) FROM PlantPhoto p WHERE p.plant.id = :plantId")
    Long countByPlantId(@Param("plantId") Long plantId);
    
    @Query("SELECT p FROM PlantPhoto p WHERE " +
           "LOWER(p.caption) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.plant.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.takenAt DESC")
    List<PlantPhoto> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM PlantPhoto p WHERE p.healthAnalysis IS NOT NULL AND " +
           "p.healthAnalysis.healthStatus = :status ORDER BY p.takenAt DESC")
    List<PlantPhoto> findByHealthStatus(@Param("status") String status);
    
    @Query("SELECT p FROM PlantPhoto p WHERE p.takenAt BETWEEN :startDate AND :endDate " +
           "ORDER BY p.takenAt DESC")
    List<PlantPhoto> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
}