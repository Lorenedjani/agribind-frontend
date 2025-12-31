package com.agribind.plant_monitoring.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthTrendDTO {
    private LocalDateTime date;
    private Double healthScore;
    private String healthStatus;

	public LocalDateTime getDate() {
		return this.date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public Double getHealthScore() {
		return this.healthScore;
	}

	public void setHealthScore(Double healthScore) {
		this.healthScore = healthScore;
	}

	public String getHealthStatus() {
		return this.healthStatus;
	}

	public void setHealthStatus(String healthStatus) {
		this.healthStatus = healthStatus;
	}

}