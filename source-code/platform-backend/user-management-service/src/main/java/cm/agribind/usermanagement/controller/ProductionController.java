package cm.agribind.usermanagement.controller;

import cm.agribind.usermanagement.dto.CreateProductionRequest;
import cm.agribind.usermanagement.dto.ProductionDashboardMetrics;
import cm.agribind.usermanagement.dto.ProductionFilterRequest;
import cm.agribind.usermanagement.dto.ProductionResponse;
import cm.agribind.usermanagement.dto.pagination.PageResponse;

import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.ProductionStatus;
import cm.agribind.usermanagement.enums.QualityGrade;
import cm.agribind.usermanagement.service.ProductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/production")
@RequiredArgsConstructor
@Tag(name = "Production Management", description = "APIs for managing agricultural production records")
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping
    @Operation(summary = "Record new production", description = "Record a new production delivery from a farmer")
    public ResponseEntity<ProductionResponse> recordProduction(
            @Valid @RequestBody CreateProductionRequest request) {
        log.info("Recording production for farmer: {}", request.getFarmerId());
        ProductionResponse response = productionService.createProduction(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get production records", description = "Get paginated list of production records with filters")
    public ResponseEntity<PageResponse<ProductionResponse>> getProductions(
            @RequestParam(required = false) CropType cropType,
            @RequestParam(required = false) QualityGrade qualityGrade,
            @RequestParam(required = false) ProductionStatus status,
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "deliveryDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        ProductionFilterRequest filter = new ProductionFilterRequest();
        filter.setCropType(cropType);
        filter.setQualityGrade(qualityGrade);
        filter.setStatus(status);
        filter.setWarehouse(warehouse);
        filter.setSearchTerm(searchTerm);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);

        PageResponse<ProductionResponse> response = productionService.getProductions(filter);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    @Operation(summary = "Search production records", description = "Search production records with advanced filters")
    public ResponseEntity<PageResponse<ProductionResponse>> searchProductions(
            @Valid @RequestBody ProductionFilterRequest filter) {
        log.info("Searching productions with filters: {}", filter);
        PageResponse<ProductionResponse> response = productionService.getProductions(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productionId}")
    @Operation(summary = "Get production by ID", description = "Get production record details by ID")
    public ResponseEntity<ProductionResponse> getProductionById(@PathVariable String productionId) {
        ProductionResponse response = productionService.getProductionById(productionId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productionId}/status")
    @Operation(summary = "Update production status", description = "Update the status of a production record")
    public ResponseEntity<ProductionResponse> updateProductionStatus(
            @PathVariable String productionId,
            @RequestParam ProductionStatus status,
            @RequestParam(required = false) String reason) {
        log.info("Updating production {} status to {}", productionId, status);
        ProductionResponse response = productionService.updateProductionStatus(productionId, status, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/metrics")
    @Operation(summary = "Get dashboard metrics", description = "Get production dashboard metrics and statistics")
    public ResponseEntity<ProductionDashboardMetrics> getDashboardMetrics() {
        log.info("Fetching production dashboard metrics");
        ProductionDashboardMetrics metrics = productionService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/crops")
    @Operation(summary = "Get available crop types", description = "Get list of all available crop types")
    public ResponseEntity<List<String>> getAvailableCropTypes() {
        List<String> crops = Arrays.stream(CropType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(crops);
    }

    @GetMapping("/grades")
    @Operation(summary = "Get quality grades", description = "Get list of all quality grades")
    public ResponseEntity<List<String>> getQualityGrades() {
        List<String> grades = Arrays.stream(QualityGrade.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get production statuses", description = "Get list of all production statuses")
    public ResponseEntity<List<String>> getProductionStatuses() {
        List<String> statuses = Arrays.stream(ProductionStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/warehouses")
    @Operation(summary = "Get warehouses", description = "Get list of available warehouses")
    public ResponseEntity<List<String>> getWarehouses() {
        List<String> warehouses = List.of(
                "Douala Warehouse",
                "Yaoundé Warehouse",
                "Garoua Warehouse",
                "Bafoussam Warehouse",
                "Bamenda Warehouse"
        );
        return ResponseEntity.ok(warehouses);
    }
}