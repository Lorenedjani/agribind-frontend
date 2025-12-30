package com.agribind.plant_monitoring.service;

import com.agribind.plant_monitoring.dto.HealthAnalysisDTO;
import com.agribind.plant_monitoring.model.HealthAnalysis;
import com.agribind.plant_monitoring.model.PlantPhoto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PlantHealthAnalysisService {
    
    private final double HEALTHY_THRESHOLD = 80.0;
    private final double MODERATE_THRESHOLD = 50.0;
    private final double POOR_THRESHOLD = 20.0;
    
    @Async
    public CompletableFuture<HealthAnalysis> analyzePlantHealth(PlantPhoto photo) {
        log.info("Starting health analysis for photo: {}", photo.getId());
        
        try {
            // Simulate AI/ML analysis - In production, integrate with actual ML model
            HealthAnalysis analysis = performImageAnalysis(photo);
            
            log.info("Health analysis completed for photo: {}", photo.getId());
            return CompletableFuture.completedFuture(analysis);
            
        } catch (Exception e) {
            log.error("Error analyzing plant health for photo: {}", photo.getId(), e);
            throw new RuntimeException("Failed to analyze plant health", e);
        }
    }
    
    private HealthAnalysis performImageAnalysis(PlantPhoto photo) throws IOException {
        HealthAnalysis analysis = new HealthAnalysis();
        analysis.setPhoto(photo);
        
        // Read image for analysis
        BufferedImage image = ImageIO.read(photo.getFilePath());
        
        // Calculate health scores (simulated - replace with actual ML model)
        double leafColorScore = analyzeLeafColor(image);
        double leafTextureScore = analyzeLeafTexture(image);
        double growthPatternScore = analyzeGrowthPattern(image);
        
        // Calculate overall health score
        double healthScore = calculateHealthScore(leafColorScore, leafTextureScore, growthPatternScore);
        analysis.setHealthScore(healthScore);
        
        // Determine health status
        analysis.setHealthStatus(determineHealthStatus(healthScore));
        
        // Detect diseases (simulated)
        String diseaseDetected = detectDiseases(image);
        if (diseaseDetected != null) {
            analysis.setDiseaseDetected(diseaseDetected);
            analysis.setDiseaseConfidence(0.85); // Simulated confidence score
        }
        
        // Check for nutrient deficiencies
        String nutrientDeficiency = checkNutrientDeficiency(image);
        analysis.setNutrientDeficiency(nutrientDeficiency);
        
        // Store individual scores
        analysis.setLeafColorScore(leafColorScore);
        analysis.setLeafTextureScore(leafTextureScore);
        analysis.setGrowthPatternScore(growthPatternScore);
        
        // Generate recommendations
        analysis.setRecommendations(generateRecommendations(analysis));
        
        // Store analysis metadata
        analysis.setAnalysisMetadata(generateAnalysisMetadata(analysis));
        
        return analysis;
    }
    
    private double analyzeLeafColor(BufferedImage image) {
        // Simulated color analysis
        // In production, use color histogram analysis
        return Math.random() * 40 + 60; // 60-100 range
    }
    
    private double analyzeLeafTexture(BufferedImage image) {
        // Simulated texture analysis
        return Math.random() * 30 + 70; // 70-100 range
    }
    
    private double analyzeGrowthPattern(BufferedImage image) {
        // Simulated growth pattern analysis
        return Math.random() * 50 + 50; // 50-100 range
    }
    
    private double calculateHealthScore(double colorScore, double textureScore, double growthScore) {
        // Weighted average
        return (colorScore * 0.4) + (textureScore * 0.3) + (growthScore * 0.3);
    }
    
    private HealthAnalysis.HealthStatus determineHealthStatus(double healthScore) {
        if (healthScore >= HEALTHY_THRESHOLD) {
            return HealthAnalysis.HealthStatus.HEALTHY;
        } else if (healthScore >= MODERATE_THRESHOLD) {
            return HealthAnalysis.HealthStatus.MODERATE;
        } else if (healthScore >= POOR_THRESHOLD) {
            return HealthAnalysis.HealthStatus.POOR;
        } else {
            return HealthAnalysis.HealthStatus.CRITICAL;
        }
    }
    
    private String detectDiseases(BufferedImage image) {
        // Simulated disease detection
        // In production, use ML model for disease classification
        double random = Math.random();
        if (random < 0.1) {
            return "Powdery Mildew";
        } else if (random < 0.15) {
            return "Leaf Spot";
        } else if (random < 0.2) {
            return "Rust";
        }
        return null;
    }
    
    private String checkNutrientDeficiency(BufferedImage image) {
        // Simulated nutrient deficiency check
        double random = Math.random();
        if (random < 0.1) {
            return "Nitrogen Deficiency";
        } else if (random < 0.15) {
            return "Potassium Deficiency";
        } else if (random < 0.18) {
            return "Phosphorus Deficiency";
        }
        return null;
    }
    
    private String generateRecommendations(HealthAnalysis analysis) {
        StringBuilder recommendations = new StringBuilder();
        
        if (analysis.getHealthScore() < HEALTHY_THRESHOLD) {
            recommendations.append("Plant health needs attention. ");
            
            if (analysis.getDiseaseDetected() != null) {
                recommendations.append(String.format(
                    "Detected %s. Consider using appropriate fungicide. ",
                    analysis.getDiseaseDetected()
                ));
            }
            
            if (analysis.getNutrientDeficiency() != null) {
                recommendations.append(String.format(
                    "%s detected. Apply balanced fertilizer. ",
                    analysis.getNutrientDeficiency()
                ));
            }
            
            if (analysis.getLeafColorScore() < 70) {
                recommendations.append("Leaf color indicates possible stress. Check watering schedule. ");
            }
            
            if (analysis.getGrowthPatternScore() < 60) {
                recommendations.append("Growth pattern suggests environmental stress. Review light and temperature conditions. ");
            }
        } else {
            recommendations.append("Plant is healthy. Continue current care regimen. ");
        }
        
        return recommendations.toString();
    }
    
    private String generateAnalysisMetadata(HealthAnalysis analysis) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("analysis_version", "1.0");
        metadata.put("analysis_timestamp", System.currentTimeMillis());
        metadata.put("confidence_scores", Map.of(
            "color_analysis", analysis.getLeafColorScore(),
            "texture_analysis", analysis.getLeafTextureScore(),
            "growth_analysis", analysis.getGrowthPatternScore()
        ));
        
        // Convert to JSON string
        return metadata.toString();
    }
    
    public HealthAnalysisDTO convertToDTO(HealthAnalysis analysis) {
        if (analysis == null) return null;
        
        HealthAnalysisDTO dto = new HealthAnalysisDTO();
        dto.setId(analysis.getId());
        dto.setHealthScore(analysis.getHealthScore());
        dto.setHealthStatus(analysis.getHealthStatus());
        dto.setDiseaseDetected(analysis.getDiseaseDetected());
        dto.setDiseaseConfidence(analysis.getDiseaseConfidence());
        dto.setLeafColorScore(analysis.getLeafColorScore());
        dto.setLeafTextureScore(analysis.getLeafTextureScore());
        dto.setGrowthPatternScore(analysis.getGrowthPatternScore());
        dto.setNutrientDeficiency(analysis.getNutrientDeficiency());
        dto.setRecommendations(analysis.getRecommendations());
        dto.setAnalyzedAt(analysis.getAnalyzedAt());
        
        return dto;
    }
}
