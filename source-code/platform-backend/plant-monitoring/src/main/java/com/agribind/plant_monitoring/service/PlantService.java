package com.agribind.plant_monitoring.service;

import com.agribind.plant_monitoring.dto.PlantHealthSummaryDTO;
import com.agribind.plant_monitoring.dto.HealthTrendDTO;
import com.agribind.plant_monitoring.model.HealthAnalysis;
import com.agribind.plant_monitoring.model.Plant;
import com.agribind.plant_monitoring.repository.HealthAnalysisRepository;
import com.agribind.plant_monitoring.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlantService {
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Autowired
    private HealthAnalysisRepository healthAnalysisRepository;
    
    public Plant createPlant(Plant plant) {
        return plantRepository.save(plant);
    }
    
    public Plant updatePlant(Long id, Plant plantDetails) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plant not found with id: " + id));
        
        plant.setName(plantDetails.getName());
        plant.setSpecies(plantDetails.getSpecies());
        plant.setPlantType(plantDetails.getPlantType());
        plant.setDescription(plantDetails.getDescription());
        plant.setOptimalTemperatureMin(plantDetails.getOptimalTemperatureMin());
        plant.setOptimalTemperatureMax(plantDetails.getOptimalTemperatureMax());
        plant.setOptimalHumidityMin(plantDetails.getOptimalHumidityMin());
        plant.setOptimalHumidityMax(plantDetails.getOptimalHumidityMax());
        plant.setOptimalLightIntensityMin(plantDetails.getOptimalLightIntensityMin());
        plant.setOptimalLightIntensityMax(plantDetails.getOptimalLightIntensityMax());
        
        return plantRepository.save(plant);
    }

    public List<Plant> searchPlantsByName(String name) {
        return plantRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Plant> getAllPlants() {
        return plantRepository.findAll();
    }
    
    public Plant getPlantById(Long id) {
        return plantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plant not found with id: " + id));
    }
    
    public void deletePlant(Long id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plant not found with id: " + id));
        plantRepository.delete(plant);
    }
    
    public PlantHealthSummaryDTO getPlantHealthSummary(Long plantId) {
        Plant plant = getPlantById(plantId);
        
        HealthAnalysis latestAnalysis = healthAnalysisRepository.findLatestByPlantId(plantId);
        Double averageHealthScore = healthAnalysisRepository.findAverageHealthScoreByPlantId(plantId);
        
        List<HealthAnalysis> recentAnalyses = healthAnalysisRepository
                .findByPhoto_PlantIdOrderByAnalyzedAtDesc(plantId)
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        
        PlantHealthSummaryDTO summary = new PlantHealthSummaryDTO();
        summary.setPlantId(plantId);
        summary.setPlantName(plant.getName());
        summary.setCurrentHealthScore(
            latestAnalysis != null ? latestAnalysis.getHealthScore() : null
        );
        summary.setCurrentHealthStatus(
            latestAnalysis != null ? latestAnalysis.getHealthStatus().name() : "UNKNOWN"
        );
        summary.setTotalPhotos(plant.getPhotos().size());
        summary.setTotalAnalyses(recentAnalyses.size());
        summary.setLastAnalysisDate(
            latestAnalysis != null ? latestAnalysis.getAnalyzedAt() : null
        );
        
        // Generate health trends
        List<HealthTrendDTO> trends = recentAnalyses.stream()
                .map(analysis -> {
                    HealthTrendDTO trend = new HealthTrendDTO();
                    trend.setDate(analysis.getAnalyzedAt());
                    trend.setHealthScore(analysis.getHealthScore());
                    trend.setHealthStatus(analysis.getHealthStatus().name());
                    return trend;
                })
                .collect(Collectors.toList());
        
        summary.setHealthTrends(trends);
        
        return summary;
    }
}