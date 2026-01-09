package com.agribind.plant_monitoring.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plants")
@Data
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(nullable = false)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String species;
    
    public String getSpecies() {
        return this.species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    @Column(name = "plant_type")
    private String plantType;

    public String getPlantType() {
        return this.plantType;
    }

    public void setPlantType(String plantType) {
        this.plantType = plantType;
    }
    
    @Column(length = 1000)
    private String description;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Column(name = "optimal_temperature_min")
    private Double optimalTemperatureMin;

    public Double getOptimalTemperatureMin() {
        return this.optimalTemperatureMin;
    }

    public void setOptimalTemperatureMin(Double optimalTemperatureMin) {
        this.optimalTemperatureMin = optimalTemperatureMin;
    }
    
    @Column(name = "optimal_temperature_max")
    private Double optimalTemperatureMax;

    public Double getOptimalTemperatureMax() {
        return this.optimalTemperatureMax;
    }

    public void setOptimalTemperatureMax(Double optimalTemperatureMax) {
        this.optimalTemperatureMax = optimalTemperatureMax;
    }
    
    @Column(name = "optimal_humidity_min")
    private Double optimalHumidityMin;

    public Double getOptimalHumidityMin() {
        return this.optimalHumidityMin;
    }

    public void setOptimalHumidityMin(Double optimalHumidityMin) {
        this.optimalHumidityMin = optimalHumidityMin;
    }
    
    @Column(name = "optimal_humidity_max")
    private Double optimalHumidityMax;

    public Double getOptimalHumidityMax() {
        return this.optimalHumidityMax;
    }

    public void setOptimalHumidityMax(Double optimalHumidityMax) {
        this.optimalHumidityMax = optimalHumidityMax;
    }
    
    @Column(name = "optimal_light_intensity_min")
    private Double optimalLightIntensityMin;

    public Double getOptimalLightIntensityMin() {
        return this.optimalLightIntensityMin;
    }

    public void setOptimalLightIntensityMin(Double optimalLightIntensityMin) {
        this.optimalLightIntensityMin = optimalLightIntensityMin;
    }
    
    @Column(name = "optimal_light_intensity_max")
    private Double optimalLightIntensityMax;

    public Double getOptimalLightIntensityMax() {
        return this.optimalLightIntensityMax;
    }

    public void setOptimalLightIntensityMax(Double optimalLightIntensityMax) {
        this.optimalLightIntensityMax = optimalLightIntensityMax;
    }

    @Column(name = "planting_date")
    private LocalDateTime plantingDate;
    
    public LocalDateTime getPlantingDate() {
        return this.plantingDate;
    }

    public void setPlantingDate(LocalDateTime plantingDate) {
        this.plantingDate = plantingDate;
    }

    
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlantPhoto> photos = new ArrayList<>();
    
    public List<PlantPhoto> getPhotos() {
        return this.photos;
    }

    public void setPhotos(List<PlantPhoto> photos) {
        this.photos = photos;
    }

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HealthRecord> healthRecords = new ArrayList<>();

    public List<HealthRecord> getHealthRecords() {
        return this.healthRecords;
    }

    public void setHealthRecords(List<HealthRecord> healthRecords) {
        this.healthRecords = healthRecords;
    }
    
}
