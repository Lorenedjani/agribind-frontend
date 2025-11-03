package cm.agribind.usermanagement.service.query;

import cm.agribind.usermanagement.dto.query.DashboardMetricsQuery;
import cm.agribind.usermanagement.dto.response.DashboardMetricsResponse;

import java.util.Map;

public interface DashboardQueryService {

    DashboardMetricsResponse getDashboardMetrics();
    DashboardMetricsResponse getDashboardMetrics(DashboardMetricsQuery query);
    Map<String, Long> getUserTypeDistribution();
    Map<String, Long> getRegionalDistribution();
    Map<String, Long> getStatusDistribution();
    Map<String, Long> getAgriculturalTypeDistribution();
    Map<String, Long> getTopCrops(int limit);
    Map<String, Long> getTopLivestock(int limit);
}