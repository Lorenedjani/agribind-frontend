package com.agribind.production_monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Flat row DTO used to populate JasperReport data source.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRowDTO {

    private String farmerId;
    private String farmerName;
    private String farmerRegion;
    private String cooperativeId;
    private String productName;
    private BigDecimal quantity;
    private String unit;
    private String qualityGrade;
    private BigDecimal unitPrice;
    private BigDecimal valueXaf;
    private LocalDate productionDate;
    private String maturityStatus;
}
