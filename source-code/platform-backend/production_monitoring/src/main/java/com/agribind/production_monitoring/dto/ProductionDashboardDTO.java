package com.agribind.production_monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionDashboardDTO {

    private Long cooperativeId;
    private String cooperativeName;
    private List<ProductionAggregateDTO> productionByType;
    private Map<String, Integer> farmersByProduct;
    private Integer totalActiveFarmers;
    private String reportPeriod;
    private Map<String, Object> statistics;
}
