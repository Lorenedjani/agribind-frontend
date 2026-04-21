package com.agribind.production_monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Analytics DTO: crop quantities grouped by region.
 * {@link #totalQuantity} is production volume; {@link #cropTypeCount} is the number of distinct crops.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropsByRegionDTO {

    /** Cameroonian region name (e.g. "CENTRE", "LITTORAL") */
    private String region;

    /** Total quantity across all crops in this region */
    private BigDecimal totalQuantity;

    /** Number of distinct farmers in this region */
    private Integer farmerCount;

    /** Number of distinct crop types in this region */
    private Integer cropTypeCount;

    /** Breakdown per crop within this region */
    private List<CropEntry> crops;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CropEntry {
        private String cropName;
        private BigDecimal quantity;
        private String unit;
        private Integer farmerCount;
    }
}
