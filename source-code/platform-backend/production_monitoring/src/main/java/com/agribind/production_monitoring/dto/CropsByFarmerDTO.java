package com.agribind.production_monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Analytics DTO: crop quantities per farmer.
 * Used for "Crops by Farmer" chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropsByFarmerDTO {

    private String farmerId;
    private String farmerName;
    private String farmerRegion;

    /** Total quantity delivered by this farmer */
    private BigDecimal totalQuantity;

    /** Total value in XAF */
    private BigDecimal totalValueXaf;

    /** Number of deliveries */
    private Integer deliveryCount;

    /** Breakdown per crop type */
    private List<CropEntry> crops;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CropEntry {
        private String cropName;
        private BigDecimal quantity;
        private String qualityGrade;
    }
}
