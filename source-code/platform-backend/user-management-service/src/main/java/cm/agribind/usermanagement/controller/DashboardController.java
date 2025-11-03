package cm.agribind.usermanagement.controller;

import cm.agribind.usermanagement.dto.query.DashboardMetricsQuery;
import cm.agribind.usermanagement.dto.response.DashboardMetricsResponse;
import cm.agribind.usermanagement.service.query.DashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for dashboard metrics and analytics")
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;

    @GetMapping("/metrics")
    @Operation(summary = "Get dashboard metrics", description = "Get comprehensive metrics for dashboard display")
    public ResponseEntity<DashboardMetricsResponse> getDashboardMetrics() {
        log.info("Fetching dashboard metrics");
        DashboardMetricsResponse response = dashboardQueryService.getDashboardMetrics();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/metrics/filtered")
    @Operation(summary = "Get filtered dashboard metrics", description = "Get dashboard metrics with date range and filters")
    public ResponseEntity<DashboardMetricsResponse> getFilteredDashboardMetrics(
            @RequestBody DashboardMetricsQuery query) {
        log.info("Fetching filtered dashboard metrics: {}", query);
        DashboardMetricsResponse response = dashboardQueryService.getDashboardMetrics(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/user-types")
    @Operation(summary = "Get user type distribution", description = "Get distribution of FARMER, COOPERATIVE, GOVERNMENT users")
    public ResponseEntity<Map<String, Long>> getUserTypeDistribution() {
        Map<String, Long> distribution = dashboardQueryService.getUserTypeDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/metrics/regions")
    @Operation(summary = "Get regional distribution", description = "Get user distribution across regions")
    public ResponseEntity<Map<String, Long>> getRegionalDistribution() {
        Map<String, Long> distribution = dashboardQueryService.getRegionalDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/metrics/status")
    @Operation(summary = "Get status distribution", description = "Get distribution of user statuses")
    public ResponseEntity<Map<String, Long>> getStatusDistribution() {
        Map<String, Long> distribution = dashboardQueryService.getStatusDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/metrics/agricultural-types")
    @Operation(summary = "Get agricultural type distribution", description = "Get distribution of agricultural types")
    public ResponseEntity<Map<String, Long>> getAgriculturalTypeDistribution() {
        Map<String, Long> distribution = dashboardQueryService.getAgriculturalTypeDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/metrics/top-crops")
    @Operation(summary = "Get top crops", description = "Get most common crops among farmers")
    public ResponseEntity<Map<String, Long>> getTopCrops(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Long> topCrops = dashboardQueryService.getTopCrops(limit);
        return ResponseEntity.ok(topCrops);
    }

    @GetMapping("/metrics/top-livestock")
    @Operation(summary = "Get top livestock", description = "Get most common livestock among farmers")
    public ResponseEntity<Map<String, Long>> getTopLivestock(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Long> topLivestock = dashboardQueryService.getTopLivestock(limit);
        return ResponseEntity.ok(topLivestock);
    }

    @GetMapping("/metrics/cooperative-stats")
    @Operation(summary = "Get cooperative statistics", description = "Get cooperative-related statistics")
    public ResponseEntity<Map<String, Object>> getCooperativeStatistics() {
        Map<String, Object> stats = Map.of(
                "totalCooperativeMembers", dashboardQueryService.getDashboardMetrics().getTotalCooperativeMembers(),
                "averageCooperativeSize", dashboardQueryService.getDashboardMetrics().getAverageCooperativeSize(),
                "cooperativesWithStorage", dashboardQueryService.getDashboardMetrics().getCooperativesWithStorage()
        );
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/metrics/registration-trends")
    @Operation(summary = "Get registration trends", description = "Get user registration trends over time")
    public ResponseEntity<Map<String, Object>> getRegistrationTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // This would be implemented in the service layer
        Map<String, Object> trends = Map.of(
                "period", startDate + " to " + endDate,
                "totalRegistrations", 150,
                "farmerRegistrations", 120,
                "cooperativeRegistrations", 20,
                "governmentRegistrations", 10
        );
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/metrics/active-users")
    @Operation(summary = "Get active users metrics", description = "Get metrics about active users")
    public ResponseEntity<Map<String, Object>> getActiveUsersMetrics() {
        DashboardMetricsResponse metrics = dashboardQueryService.getDashboardMetrics();
        Map<String, Object> activeMetrics = Map.of(
                "totalActiveUsers", metrics.getActiveUsers(),
                "activeFarmers", metrics.getTotalFarmers(),
                "activeCooperatives", metrics.getTotalCooperatives(),
                "activeGovernmentOfficials", metrics.getTotalGovernmentOfficials(),
                "activityRate", String.format("%.2f%%", (metrics.getActiveUsers().doubleValue() / metrics.getTotalUsers().doubleValue()) * 100)
        );
        return ResponseEntity.ok(activeMetrics);
    }
}