package com.agribind.plant_monitoring.repository;

import com.agribind.plant_monitoring.model.PlantPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlantPhotoRepository extends JpaRepository<PlantPhoto, Long>, JpaSpecificationExecutor<PlantPhoto> {
    
    List<PlantPhoto> findByPlantIdOrderByTakenAtDesc(Long plantId);
    
    @Query("SELECT p FROM PlantPhoto p WHERE p.plant.id = :plantId AND " +
           "DATE(p.takenAt) = DATE(:date) ORDER BY p.takenAt DESC")
    List<PlantPhoto> findByPlantIdAndDate(@Param("plantId") Long plantId, 
                                          @Param("date") LocalDateTime date);
    
    PlantPhoto findFirstByPlantIdOrderByTakenAtDesc(Long plantId);
}