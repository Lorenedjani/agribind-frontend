package com.agribind.production_monitoring.controller;

import com.agribind.production_monitoring.model.MaturityStatus;
import com.agribind.production_monitoring.model.ProductionRecord;
import com.agribind.production_monitoring.dto.*;
import com.agribind.production_monitoring.service.ProductionMonitoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/production")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductionMonitoringController {

    private final ProductionMonitoringService productionMonitoringService;
    //private static final Logger log = LoggerFactory.getLogger(ProductionMonitoringController.class);

    /**
     * Record new production entry
     * POST /api/v1/production/record
     */
    @PostMapping("/record")
    public ResponseEntity<ApiResponse<ProductionRecord>> recordProduction(
            @Valid @RequestBody ProductionRecordDTO dto
    ) {
        try {
            log.info("Received production record request for farmer: {}", dto.getFarmerId());
            ProductionRecord record = productionMonitoringService.recordProduction(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(record, "Production recorded successfully"));
        } catch (Exception e) {
            log.error("Error recording production: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to record production: " + e.getMessage(), "PRODUCTION_RECORD_ERROR"));
        }
    }

    /**
     * Update maturity status
     * PUT /api/v1/production/maturity
     */
    @PutMapping("/maturity")
    public ResponseEntity<ApiResponse<ProductionRecord>> updateMaturityStatus(
            @Valid @RequestBody MaturityUpdateDTO dto
    ) {
        try {
            log.info("Updating maturity status for production record: {}", dto.getProductionRecordId());
            ProductionRecord record = productionMonitoringService.updateMaturityStatus(dto);
            return ResponseEntity.ok(ApiResponse.success(record, "Maturity status updated successfully"));
        } catch (RuntimeException e) {
            log.error("Error updating maturity status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PRODUCTION_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error updating maturity status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update maturity status", "MATURITY_UPDATE_ERROR"));
        }
    }

    /**
     * Get production aggregate for a specific product
     * GET /api/v1/production/aggregate/{cooperativeId}/{productName}
     */
    @GetMapping("/aggregate/{cooperativeId}/{productName}")
    public ResponseEntity<ApiResponse<ProductionAggregateDTO>> getProductionAggregate(
            @PathVariable Long cooperativeId,
            @PathVariable String productName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            log.info("Fetching production aggregate for cooperative: {}, product: {}", cooperativeId, productName);
            ProductionAggregateDTO aggregate = productionMonitoringService.getProductionAggregate(
                    cooperativeId, productName, startDate, endDate
            );
            return ResponseEntity.ok(ApiResponse.success(aggregate, "Production aggregate retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching production aggregate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve production aggregate", "AGGREGATE_FETCH_ERROR"));
        }
    }

    /**
     * Get production dashboard for cooperative
     * GET /api/v1/production/dashboard/{cooperativeId}
     */
    @GetMapping("/dashboard/{cooperativeId}")
    public ResponseEntity<ApiResponse<ProductionDashboardDTO>> getProductionDashboard(
            @PathVariable Long cooperativeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            log.info("Generating production dashboard for cooperative: {}", cooperativeId);
            ProductionDashboardDTO dashboard = productionMonitoringService.getProductionDashboard(
                    cooperativeId, startDate, endDate
            );
            return ResponseEntity.ok(ApiResponse.success(dashboard, "Production dashboard generated successfully"));
        } catch (Exception e) {
            log.error("Error generating production dashboard: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate production dashboard", "DASHBOARD_ERROR"));
        }
    }

    /**
     * Get farmer's production history
     * GET /api/v1/production/farmer/{farmerId}/history
     */
    @GetMapping("/farmer/{farmerId}/history")
    public ResponseEntity<ApiResponse<List<ProductionRecord>>> getFarmerProductionHistory(
            @PathVariable Long farmerId
    ) {
        try {
            log.info("Fetching production history for farmer: {}", farmerId);
            List<ProductionRecord> history = productionMonitoringService.getFarmerProductionHistory(farmerId);
            return ResponseEntity.ok(ApiResponse.success(history, "Production history retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching farmer production history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve production history", "HISTORY_FETCH_ERROR"));
        }
    }

    /**
     * Get products by maturity status
     * GET /api/v1/production/cooperative/{cooperativeId}/maturity
     */
    @GetMapping("/cooperative/{cooperativeId}/maturity")
    public ResponseEntity<ApiResponse<List<ProductionRecord>>> getProductsByMaturityStatus(
            @PathVariable Long cooperativeId,
            @RequestParam String productName,
            @RequestParam MaturityStatus status
    ) {
        try {
            log.info("Fetching products by maturity status for cooperative: {}, product: {}, status: {}",
                    cooperativeId, productName, status);
            List<ProductionRecord> products = productionMonitoringService.getProductsByMaturityStatus(
                    cooperativeId, productName, status
            );
            return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching products by maturity status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve products", "PRODUCTS_FETCH_ERROR"));
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/production/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("OK", "Production monitoring service is running"));
    }
}
