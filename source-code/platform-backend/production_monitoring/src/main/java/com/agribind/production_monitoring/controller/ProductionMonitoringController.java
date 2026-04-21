package com.agribind.production_monitoring.controller;

import com.agribind.production_monitoring.model.MaturityStatus;
import com.agribind.production_monitoring.model.ProductionRecord;
import com.agribind.production_monitoring.dto.*;
import com.agribind.production_monitoring.security.ProductionAnalyticsAuthorizationService;
import com.agribind.production_monitoring.service.ProductionMonitoringService;
import com.agribind.production_monitoring.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    private final ReportService reportService;
    private final ProductionAnalyticsAuthorizationService authorizationService;

    /**
     * Record new production entry
     * POST /api/v1/production/record
     */
    @Operation(summary = "Record production", description = "Record a new production entry for a farmer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Production recorded successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        try {
            String coop = authorizationService.resolveCooperativePathOrParam(cooperativeId, authentication);
            log.info("Fetching production aggregate for cooperative: {}, product: {}", coop, productName);
            ProductionAggregateDTO aggregate = productionMonitoringService.getProductionAggregate(
                    coop, productName, startDate, endDate
            );
            return ResponseEntity.ok(ApiResponse.success(aggregate, "Production aggregate retrieved successfully"));
        } catch (AccessDeniedException | ResponseStatusException e) {
            HttpStatus st = e instanceof ResponseStatusException rse
                    ? HttpStatus.valueOf(rse.getStatusCode().value())
                    : HttpStatus.FORBIDDEN;
            String msg = e instanceof ResponseStatusException rse && rse.getReason() != null ? rse.getReason() : e.getMessage();
            return ResponseEntity.status(st).body(ApiResponse.error(msg, "FORBIDDEN"));
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        try {
            String coop = authorizationService.resolveCooperativePathOrParam(cooperativeId, authentication);
            log.info("Generating production dashboard for cooperative: {}", coop);
            ProductionDashboardDTO dashboard = productionMonitoringService.getProductionDashboard(
                    coop, startDate, endDate
            );
            return ResponseEntity.ok(ApiResponse.success(dashboard, "Production dashboard generated successfully"));
        } catch (AccessDeniedException | ResponseStatusException e) {
            HttpStatus st = e instanceof ResponseStatusException rse
                    ? HttpStatus.valueOf(rse.getStatusCode().value())
                    : HttpStatus.FORBIDDEN;
            String msg = e instanceof ResponseStatusException rse && rse.getReason() != null ? rse.getReason() : e.getMessage();
            return ResponseEntity.status(st).body(ApiResponse.error(msg, "FORBIDDEN"));
        } catch (Exception e) {
            log.error("Error generating production dashboard: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate production dashboard", "DASHBOARD_ERROR"));
        }
    }
    /**
     * Get analytics: Crops by Region
     * GET /api/v1/production/analytics/crops-by-region
     */
    @GetMapping("/analytics/crops-by-region")
    public ResponseEntity<ApiResponse<List<CropsByRegionDTO>>> getCropsByRegion(
            @RequestParam(required = false) String cooperativeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        try {
            String scoped = authorizationService.resolveCooperativeId(cooperativeId, authentication);
            log.info("Fetching crops by region for cooperative: {}", scoped);
            List<CropsByRegionDTO> stats = productionMonitoringService.getCropsByRegion(scoped, startDate, endDate);
            stats = authorizationService.filterCropsByRegionForRole(stats, authentication);
            return ResponseEntity.ok(ApiResponse.success(stats, "Crops by region retrieved successfully"));
        } catch (AccessDeniedException | ResponseStatusException e) {
            HttpStatus st = e instanceof ResponseStatusException rse
                    ? HttpStatus.valueOf(rse.getStatusCode().value())
                    : HttpStatus.FORBIDDEN;
            String msg = e instanceof ResponseStatusException rse && rse.getReason() != null ? rse.getReason() : e.getMessage();
            return ResponseEntity.status(st).body(ApiResponse.error(msg, "FORBIDDEN"));
        } catch (Exception e) {
            log.error("Error fetching crops by region: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve crops by region: " + e.getMessage(), "ANALYTICS_ERROR"));
        }
    }

    /**
     * Get analytics: Crops by Farmer
     * GET /api/v1/production/analytics/crops-by-farmer
     */
    @GetMapping("/analytics/crops-by-farmer")
    public ResponseEntity<ApiResponse<List<CropsByFarmerDTO>>> getCropsByFarmer(
            @RequestParam(required = false) String cooperativeId,
            @RequestParam(defaultValue = "All Regions") String region,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        try {
            String scoped = authorizationService.resolveCooperativeId(cooperativeId, authentication);
            log.info("Fetching crops by farmer for cooperative: {}, region: {}", scoped, region);
            List<CropsByFarmerDTO> stats = productionMonitoringService.getCropsByFarmer(scoped, region, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(stats, "Crops by farmer retrieved successfully"));
        } catch (AccessDeniedException | ResponseStatusException e) {
            HttpStatus st = e instanceof ResponseStatusException rse
                    ? HttpStatus.valueOf(rse.getStatusCode().value())
                    : HttpStatus.FORBIDDEN;
            String msg = e instanceof ResponseStatusException rse && rse.getReason() != null ? rse.getReason() : e.getMessage();
            return ResponseEntity.status(st).body(ApiResponse.error(msg, "FORBIDDEN"));
        } catch (Exception e) {
            log.error("Error fetching crops by farmer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve crops by farmer: " + e.getMessage(), "ANALYTICS_ERROR"));
        }
    }

    /**
     * Generate Farmer Production Report (PDF or Excel)
     * GET /api/v1/production/reports/farmer-production
     */
    @GetMapping("/reports/farmer-production")
    public ResponseEntity<byte[]> generateFarmerProductionReport(
            @RequestParam(required = false) String cooperativeId,
            @RequestParam(defaultValue = "All Regions") String region,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "pdf") String format,
            Authentication authentication
    ) {
        try {
            String scoped = authorizationService.resolveCooperativeId(cooperativeId, authentication);
            byte[] reportFile = reportService.generateFarmerProductionReport(
                    scoped, region, startDate, endDate, format);

            HttpHeaders headers = new HttpHeaders();
            if ("excel".equalsIgnoreCase(format)) {
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDispositionFormData("attachment", "Farmer_Production_Report.xlsx");
            } else {
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("inline", "Farmer_Production_Report.pdf");
            }

            return new ResponseEntity<>(reportFile, headers, HttpStatus.OK);

        } catch (AccessDeniedException | ResponseStatusException e) {
            return ResponseEntity.status(e instanceof ResponseStatusException rse
                    ? HttpStatus.valueOf(rse.getStatusCode().value())
                    : HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            @RequestParam MaturityStatus status,
            Authentication authentication
    ) {
        try {
            String coop = authorizationService.resolveCooperativePathOrParam(cooperativeId, authentication);
            log.info("Fetching products by maturity status for cooperative: {}, product: {}, status: {}",
                    coop, productName, status);
            List<ProductionRecord> products = productionMonitoringService.getProductsByMaturityStatus(
                    coop, productName, status
            );
            return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
        } catch (AccessDeniedException | ResponseStatusException e) {
            HttpStatus st = e instanceof ResponseStatusException rse
                    ? HttpStatus.valueOf(rse.getStatusCode().value())
                    : HttpStatus.FORBIDDEN;
            String msg = e instanceof ResponseStatusException rse && rse.getReason() != null ? rse.getReason() : e.getMessage();
            return ResponseEntity.status(st).body(ApiResponse.error(msg, "FORBIDDEN"));
        } catch (Exception e) {
            log.error("Error fetching products by maturity status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve products", "PRODUCTS_FETCH_ERROR"));
        }
    }

    /**
     * Get all production records for a cooperative with pagination
     * GET /api/v1/production/cooperative/{cooperativeId}
     */
    @GetMapping("/cooperative/{cooperativeId}")
    public ResponseEntity<ApiResponse<Page<ProductionRecord>>> getProductionRecords(
            @PathVariable String cooperativeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String qualityGrade,
            @RequestParam(required = false) String searchTerm,
            Authentication authentication
    ) {
        try {
            String coop = authorizationService.resolveCooperativePathOrParam(cooperativeId, authentication);
            log.info("Fetching production records for cooperative: {}, page: {}, size: {}", coop, page, size);
            Page<ProductionRecord> records = productionMonitoringService.getProductionRecords(
                    coop, page, size, productName, qualityGrade, searchTerm
            );
            return ResponseEntity.ok(ApiResponse.success(records, "Production records retrieved successfully"));
        } catch (AccessDeniedException | ResponseStatusException e) {
            HttpStatus st = e instanceof ResponseStatusException rse
                    ? HttpStatus.valueOf(rse.getStatusCode().value())
                    : HttpStatus.FORBIDDEN;
            String msg = e instanceof ResponseStatusException rse && rse.getReason() != null ? rse.getReason() : e.getMessage();
            return ResponseEntity.status(st).body(ApiResponse.error(msg, "FORBIDDEN"));
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