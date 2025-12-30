package com.agribind.plant_monitoring.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
class HealthTrendDTO {
    private LocalDateTime date;
    private Double healthScore;
    private String healthStatus;
}