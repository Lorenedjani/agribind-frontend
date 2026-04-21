package com.agribind.plant_monitoring.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
@Data
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    public Plant getPlant() {
        return this.plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    @Column(name = "temperature")
    private Double temperature;

    public Double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    @Column(name = "humidity")
    private Double humidity;

    public Double getHumidity() {
        return this.humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }
    
    @Column(name = "soil_moisture")
    private Double soilMoisture;

    public Double getSoilMoisture() {
        return this.soilMoisture;
    }

    public void setSoilMoisture(Double soilMoisture) {
        this.soilMoisture = soilMoisture;
    }

    @Column(name = "light_intensity")
    private Double lightIntensity;


    public Double getLightIntensity() {
        return this.lightIntensity;
    }

    public void setLightIntensity(Double lightIntensity) {
        this.lightIntensity = lightIntensity;
    }
    
    @Column(name = "ph_level")
    private Double phLevel;

    public Double getPhLevel() {
        return this.phLevel;
    }

    public void setPhLevel(Double phLevel) {
        this.phLevel = phLevel;
    }
    
    @Column(name = "health_score")
    private Double healthScore;

    public Double getHealthScore() {
        return this.healthScore;
    }

    public void setHealthScore(Double healthScore) {
        this.healthScore = healthScore;
    }
    
    @Column(length = 1000)
    private String notes;

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    public LocalDateTime getRecordedAt() {
        return this.recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}