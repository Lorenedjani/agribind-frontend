package cm.agribind.usermanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ProductionDashboardMetrics {
    private String totalProduction; // e.g., "287.5 MT"
    private String totalProductionPercent; // e.g., "+15.2% vs last cycle"
    private Integer activeFarmers;
    private String activeFarmersParticipation; // e.g., "81.7% participation rate"
    private String gradeAProduction; // e.g., "168.3 MT"
    private String gradeAPercent; // e.g., "58.5% premium quality"
    private String thisMonthDeliveries; // e.g., "42.8 MT"
    private String thisMonthChange; // e.g., "+8.3% vs last month"

    // Additional metrics
    private Map<String, Double> cropDistribution;
    private Map<String, Long> statusDistribution;
    private Map<String, Double> gradeDistribution;
    private Map<String, Double> warehouseDistribution;
}
