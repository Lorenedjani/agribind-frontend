package com.agribind.plant_monitoring.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlantHealthSummaryDTO {
    private Long plantId;
    private String plantName;
    private Double currentHealthScore;
    private String currentHealthStatus;
    private Integer totalPhotos;
    private Integer totalAnalyses;
    private LocalDateTime lastAnalysisDate;
    private List<HealthTrendDTO> healthTrends;

	public Long getPlantId() {
		return this.plantId;
	}

	public void setPlantId(Long plantId) {
		this.plantId = plantId;
	}

	public String getPlantName() {
		return this.plantName;
	}

	public void setPlantName(String plantName) {
		this.plantName = plantName;
	}

	public Double getCurrentHealthScore() {
		return this.currentHealthScore;
	}

	public void setCurrentHealthScore(Double currentHealthScore) {
		this.currentHealthScore = currentHealthScore;
	}

	public String getCurrentHealthStatus() {
		return this.currentHealthStatus;
	}

	public void setCurrentHealthStatus(String currentHealthStatus) {
		this.currentHealthStatus = currentHealthStatus;
	}

	public Integer getTotalPhotos() {
		return this.totalPhotos;
	}

	public void setTotalPhotos(Integer totalPhotos) {
		this.totalPhotos = totalPhotos;
	}

	public Integer getTotalAnalyses() {
		return this.totalAnalyses;
	}

	public void setTotalAnalyses(Integer totalAnalyses) {
		this.totalAnalyses = totalAnalyses;
	}

	public LocalDateTime getLastAnalysisDate() {
		return this.lastAnalysisDate;
	}

	public void setLastAnalysisDate(LocalDateTime lastAnalysisDate) {
		this.lastAnalysisDate = lastAnalysisDate;
	}

	public List<HealthTrendDTO> getHealthTrends() {
		return this.healthTrends;
	}

	public void setHealthTrends(List<HealthTrendDTO> healthTrends) {
		this.healthTrends = healthTrends;
	}

}

