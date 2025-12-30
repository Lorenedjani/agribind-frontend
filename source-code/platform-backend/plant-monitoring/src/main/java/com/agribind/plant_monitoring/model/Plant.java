package com.plantmonitoring.model;

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
    
    @Column(nullable = false)
    private String name;
    
    private String species;
    
    @Column(name = "plant_type")
    private String plantType;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "optimal_temperature_min")
    private Double optimalTemperatureMin;
    
    @Column(name = "optimal_temperature_max")
    private Double optimalTemperatureMax;
    
    @Column(name = "optimal_humidity_min")
    private Double optimalHumidityMin;
    
    @Column(name = "optimal_humidity_max")
    private Double optimalHumidityMax;
    
    @Column(name = "optimal_light_intensity_min")
    private Double optimalLightIntensityMin;
    
    @Column(name = "optimal_light_intensity_max")
    private Double optimalLightIntensityMax;
    
    @Column(name = "planting_date")
    private LocalDateTime plantingDate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlantPhoto> photos = new ArrayList<>();
    
    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HealthRecord> healthRecords = new ArrayList<>();
}
