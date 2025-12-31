package com.agribind.plant_monitoring.dto;

import com.agribind.plant_monitoring.model.HealthAnalysis.HealthStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HealthAnalysisDTO {
    private Long id;
    private Double healthScore;
    private HealthStatus healthStatus;
    private String diseaseDetected;
    private Double diseaseConfidence;
    private Double leafColorScore;
    private Double leafTextureScore;
    private Double growthPatternScore;
    private String nutrientDeficiency;
    private String recommendations;
    private LocalDateTime analyzedAt;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getHealthScore() {
		return this.healthScore;
	}

	public void setHealthScore(Double healthScore) {
		this.healthScore = healthScore;
	}

	public HealthStatus getHealthStatus() {
		return this.healthStatus;
	}

	public void setHealthStatus(HealthStatus healthStatus) {
		this.healthStatus = healthStatus;
	}

	public String getDiseaseDetected() {
		return this.diseaseDetected;
	}

	public void setDiseaseDetected(String diseaseDetected) {
		this.diseaseDetected = diseaseDetected;
	}

	public Double getDiseaseConfidence() {
		return this.diseaseConfidence;
	}

	public void setDiseaseConfidence(Double diseaseConfidence) {
		this.diseaseConfidence = diseaseConfidence;
	}

	public Double getLeafColorScore() {
		return this.leafColorScore;
	}

	public void setLeafColorScore(Double leafColorScore) {
		this.leafColorScore = leafColorScore;
	}

	public Double getLeafTextureScore() {
		return this.leafTextureScore;
	}

	public void setLeafTextureScore(Double leafTextureScore) {
		this.leafTextureScore = leafTextureScore;
	}

	public Double getGrowthPatternScore() {
		return this.growthPatternScore;
	}

	public void setGrowthPatternScore(Double growthPatternScore) {
		this.growthPatternScore = growthPatternScore;
	}

	public String getNutrientDeficiency() {
		return this.nutrientDeficiency;
	}

	public void setNutrientDeficiency(String nutrientDeficiency) {
		this.nutrientDeficiency = nutrientDeficiency;
	}

	public String getRecommendations() {
		return this.recommendations;
	}

	public void setRecommendations(String recommendations) {
		this.recommendations = recommendations;
	}

	public LocalDateTime getAnalyzedAt() {
		return this.analyzedAt;
	}

	public void setAnalyzedAt(LocalDateTime analyzedAt) {
		this.analyzedAt = analyzedAt;
	}


    // Additional methods like constructors, if needed, can be added here
    public HealthAnalysisDTO() {
    }

    public HealthAnalysisDTO(Long id, Double healthScore, HealthStatus healthStatus, String diseaseDetected,
                             Double diseaseConfidence, Double leafColorScore, Double leafTextureScore,
                             Double growthPatternScore, String nutrientDeficiency, String recommendations,
                             LocalDateTime analyzedAt) {
        this.id = id;
        this.healthScore = healthScore;
        this.healthStatus = healthStatus;
        this.diseaseDetected = diseaseDetected;
        this.diseaseConfidence = diseaseConfidence;
        this.leafColorScore = leafColorScore;
        this.leafTextureScore = leafTextureScore;
        this.growthPatternScore = growthPatternScore;
        this.nutrientDeficiency = nutrientDeficiency;
        this.recommendations = recommendations;
        this.analyzedAt = analyzedAt;
    }

}