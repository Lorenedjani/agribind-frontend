package com.agribind.plant_monitoring.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_analyses")
@Data
public class HealthAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    private PlantPhoto photo;
    
    @Column(name = "health_score", nullable = false)
    private Double healthScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false)
    private HealthStatus healthStatus;
    
    @Column(name = "disease_detected")
    private String diseaseDetected;
    
    @Column(name = "disease_confidence")
    private Double diseaseConfidence;
    
    @Column(name = "leaf_color_score")
    private Double leafColorScore;
    
    @Column(name = "leaf_texture_score")
    private Double leafTextureScore;
    
    @Column(name = "growth_pattern_score")
    private Double growthPatternScore;
    
    @Column(name = "nutrient_deficiency")
    private String nutrientDeficiency;
    
    @Column(length = 2000)
    private String recommendations;
    
    @Column(name = "analysis_metadata", columnDefinition = "TEXT")
    private String analysisMetadata;
    
    @CreationTimestamp
    @Column(name = "analyzed_at", updatable = false)
    private LocalDateTime analyzedAt;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PlantPhoto getPhoto() {
		return this.photo;
	}

	public void setPhoto(PlantPhoto photo) {
		this.photo = photo;
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

	
	public String getAnalysisMetadata() {
		return this.analysisMetadata;
	}

	public void setAnalysisMetadata(String analysisMetadata) {
		this.analysisMetadata = analysisMetadata;
	}

	public LocalDateTime getAnalyzedAt() {
		return this.analyzedAt;
	}

	public void setAnalyzedAt(LocalDateTime analyzedAt) {
		this.analyzedAt = analyzedAt;
	}

    
    public enum HealthStatus {
        HEALTHY, MODERATE, POOR, CRITICAL
    }
}
