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
    
    public enum HealthStatus {
        HEALTHY, MODERATE, POOR, CRITICAL
    }
}
