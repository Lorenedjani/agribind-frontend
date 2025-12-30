package com.agribind.plant_monitoring.dto;

import com.plantmonitoring.model.HealthAnalysis.HealthStatus;
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
}