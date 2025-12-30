package com.plantmonitoring.model;

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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "humidity")
    private Double humidity;
    
    @Column(name = "soil_moisture")
    private Double soilMoisture;
    
    @Column(name = "light_intensity")
    private Double lightIntensity;
    
    @Column(name = "ph_level")
    private Double phLevel;
    
    @Column(name = "health_score")
    private Double healthScore;
    
    @Column(length = 1000)
    private String notes;
    
    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;
}