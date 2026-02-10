package com.agribind.production_monitoring.controller;

import com.agribind.production_monitoring.model.MaturityStatus;
import com.agribind.production_monitoring.model.ProductionRecord;
import com.agribind.production_monitoring.dto.*;
import com.agribind.production_monitoring.service.ProductionMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
@Tag(name = "Production Monitoring Service", description = "API for managing production records, farmer contributions, maturity updates, and production aggregates")
public class ProductionMonitoringController {

    private final ProductionMonitoringService productionMonitoringService;

    /**
     * Record new production entry
     * POST /api/v1/production/record
     */
    @Operation(summary = "Record production", description = "Record a new production entry for a farmer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Production recorded successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/record")
    public ResponseEntity<ApiResponse<ProductionRecord>> recordProduction(
            @Parameter(description = "Production record details", required = true)
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
            @PathVariable String cooperativeId,
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
            @PathVariable String cooperativeId,
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
            @PathVariable String farmerId
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
            @PathVariable String cooperativeId,
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
     * Get all production records for a cooperative with pagination
     * GET /api/v1/production/cooperative/{cooperativeId}
     *
     * FIXED: Changed cooperativeId parameter type from Long to String
     */
    @GetMapping("/cooperative/{cooperativeId}")
    public ResponseEntity<ApiResponse<Page<ProductionRecord>>> getProductionRecords(
            @PathVariable String cooperativeId,  // ✅ FIXED: Changed from Long to String
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String qualityGrade,
            @RequestParam(required = false) String searchTerm
    ) {
        try {
            log.info("Fetching production records for cooperative: {}, page: {}, size: {}", cooperativeId, page, size);
            Page<ProductionRecord> records = productionMonitoringService.getProductionRecords(
                    cooperativeId, page, size, productName, qualityGrade, searchTerm
            );
            return ResponseEntity.ok(ApiResponse.success(records, "Production records retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching production records: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve production records", "RECORDS_FETCH_ERROR"));
        }
    }

    /**
     * Get production record by ID
     * GET /api/v1/production/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionRecord>> getProductionRecordById(@PathVariable Long id) {
        try {
            log.info("Fetching production record with ID: {}", id);
            ProductionRecord record = productionMonitoringService.getProductionRecordById(id);
            return ResponseEntity.ok(ApiResponse.success(record, "Production record retrieved successfully"));
        } catch (RuntimeException e) {
            log.error("Error fetching production record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PRODUCTION_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error fetching production record: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve production record", "RECORD_FETCH_ERROR"));
        }
    }

    /**
     * Update production record
     * PUT /api/v1/production/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionRecord>> updateProductionRecord(
            @PathVariable Long id,
            @Valid @RequestBody ProductionRecordDTO dto
    ) {
        try {
            log.info("Updating production record with ID: {}", id);
            ProductionRecord record = productionMonitoringService.updateProductionRecord(id, dto);
            return ResponseEntity.ok(ApiResponse.success(record, "Production record updated successfully"));
        } catch (RuntimeException e) {
            log.error("Error updating production record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PRODUCTION_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error updating production record: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update production record", "UPDATE_ERROR"));
        }
    }

    /**
     * Delete production record
     * DELETE /api/v1/production/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductionRecord(@PathVariable Long id) {
        try {
            log.info("Deleting production record with ID: {}", id);
            productionMonitoringService.deleteProductionRecord(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Production record deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting production record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PRODUCTION_NOT_FOUND"));
        } catch (Exception e) {
            log.error("Error deleting production record: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete production record", "DELETE_ERROR"));
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