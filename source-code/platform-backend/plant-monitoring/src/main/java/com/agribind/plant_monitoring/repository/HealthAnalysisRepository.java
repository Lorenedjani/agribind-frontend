package com.agribind.plant_monitoring.repository;

import com.agribind.plant_monitoring.model.HealthAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthAnalysisRepository extends JpaRepository<HealthAnalysis, Long> {
    HealthAnalysis findByPhotoId(Long photoId);
    
    List<HealthAnalysis> findByPhoto_PlantIdOrderByAnalyzedAtDesc(Long plantId);
    
    @Query("SELECT ha FROM HealthAnalysis ha WHERE ha.photo.plant.id = :plantId " +
           "ORDER BY ha.analyzedAt DESC LIMIT 1")
    HealthAnalysis findLatestByPlantId(@Param("plantId") Long plantId);
    
    @Query("SELECT AVG(ha.healthScore) FROM HealthAnalysis ha " +
           "WHERE ha.photo.plant.id = :plantId")
    Double findAverageHealthScoreByPlantId(@Param("plantId") Long plantId);
}
