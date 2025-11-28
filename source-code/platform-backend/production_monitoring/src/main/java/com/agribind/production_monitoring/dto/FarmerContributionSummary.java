package com.agribind.production_monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerContributionSummary {

    private Long farmerId;
    private String farmerName;
    private String farmerRegistrationNumber;
    private BigDecimal quantityContributed;
    private String unit;
    private Integer contributionCount;
    private Double percentageOfTotal;
}