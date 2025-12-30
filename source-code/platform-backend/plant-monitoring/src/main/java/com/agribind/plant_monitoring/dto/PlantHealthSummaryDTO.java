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
}

