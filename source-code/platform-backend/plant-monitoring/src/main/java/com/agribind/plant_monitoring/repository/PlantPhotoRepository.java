package com.agribind.plant_monitoring.repository;

import com.agribind.plant_monitoring.Model.PlantPhoto;
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
           "DATE(p.takenAt) = DATE(:date)")
    List<PlantPhoto> findByPlantIdAndDate(@Param("plantId") Long plantId, 
                                          @Param("date") LocalDateTime date);
    
    PlantPhoto findFirstByPlantIdOrderByTakenAtDesc(Long plantId);
}