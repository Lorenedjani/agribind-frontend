package com.agribind.production_monitoring.dto;

import com.agribind.production_monitoring.model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionAggregateDTO {

    private String cooperativeId;
    private String cooperativeName;
    private String productName;
    private ProductType productType;
    private BigDecimal totalQuantity;
    private String unit;
    private Integer totalFarmers;
    private List<FarmerContributionSummary> topContributors;
    private String periodDescription;
}

