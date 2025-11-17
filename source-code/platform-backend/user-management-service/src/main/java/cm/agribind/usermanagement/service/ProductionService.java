package cm.agribind.usermanagement.service;

import cm.agribind.usermanagement.dto.CreateProductionRequest;
import cm.agribind.usermanagement.dto.ProductionDashboardMetrics;
import cm.agribind.usermanagement.dto.ProductionFilterRequest;
import cm.agribind.usermanagement.dto.ProductionResponse;
import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.entity.Farmer;
import cm.agribind.usermanagement.entity.ProductionRecord;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.ProductionStatus;
import cm.agribind.usermanagement.enums.QualityGrade;
import cm.agribind.usermanagement.exception.UserNotFoundException;
import cm.agribind.usermanagement.repository.FarmerRepository;
import cm.agribind.usermanagement.repository.ProductionRepository;
import cm.agribind.usermanagement.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductionService {

    private final ProductionRepository productionRepository;
    private final FarmerRepository farmerRepository;

    // Crop prices per MT in XAF
    private static final Map<CropType, Double> CROP_PRICES = Map.of(
            CropType.COCOA, 2100000.0,
            CropType.COFFEE, 2200000.0,
            CropType.MAIZE, 500000.0,
            CropType.PALM_OIL, 1600000.0,
            CropType.COTTON, 1300000.0,
            CropType.CASSAVA, 500000.0,
            CropType.RICE, 600000.0,
            CropType.BANANAS, 450000.0
    );

    public ProductionResponse createProduction(CreateProductionRequest request) {
        log.info("Creating production record for farmer: {}", request.getFarmerId());

        // Find farmer
        Farmer farmer = farmerRepository.findById(Long.valueOf(request.getFarmerId()))
                .orElseThrow(() -> new UserNotFoundException("Farmer not found: " + request.getFarmerId()));

        // Create production record
        ProductionRecord production = new ProductionRecord();
        production.setProductionId(generateProductionId());
        production.setFarmer(farmer);
        production.setCropType(request.getCropType());
        production.setQuantity(request.getQuantity());
        production.setQualityGrade(request.getQualityGrade());
        production.setWarehouse(request.getWarehouse());
        production.setDeliveryDate(request.getDeliveryDate());
        production.setNotes(request.getNotes());
        production.setStatus(ProductionStatus.PENDING);

        // Calculate value
        Double pricePerMt = CROP_PRICES.getOrDefault(request.getCropType(), 1000000.0);
        production.setPricePerMt(pricePerMt);
        production.setValueXaf(request.getQuantity() * pricePerMt);

        ProductionRecord saved = productionRepository.save(production);
        log.info("Production record created: {}", saved.getProductionId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductionResponse> getProductions(ProductionFilterRequest filter) {
        log.debug("Fetching productions with filter: {}");

        Specification<ProductionRecord> spec = buildSpecification(filter);
        Pageable pageable = PaginationUtil.createPageable(
                filter.getPage(),
                filter.getSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<ProductionRecord> page = productionRepository.findAll(spec, pageable);

        List<ProductionResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductionResponse getProductionById(String productionId) {
        ProductionRecord production = productionRepository.findByProductionId(productionId)
                .orElseThrow(() -> new RuntimeException("Production not found: " + productionId));
        return toResponse(production);
    }

    public ProductionResponse updateProductionStatus(String productionId, ProductionStatus status, String reason) {
        log.info("Updating production {} status to {}", productionId, status);

        ProductionRecord production = productionRepository.findByProductionId(productionId)
                .orElseThrow(() -> new RuntimeException("Production not found: " + productionId));

        production.setStatus(status);

        if (status == ProductionStatus.VERIFIED) {
            production.setVerificationDate(LocalDate.now());
            // In real app, get from security context
            production.setVerifiedBy("SYSTEM");
        } else if (status == ProductionStatus.REJECTED && reason != null) {
            production.setRejectionReason(reason);
        }

        ProductionRecord updated = productionRepository.save(production);
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public ProductionDashboardMetrics getDashboardMetrics() {
        log.debug("Calculating production dashboard metrics");

        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate lastMonthStart = firstDayOfMonth.minusMonths(1);
        LocalDate lastMonthEnd = lastMonthStart.with(TemporalAdjusters.lastDayOfMonth());

        // Total production
        Double totalProduction = productionRepository.getTotalVerifiedProduction();
        if (totalProduction == null) totalProduction = 0.0;

        // Grade A production
        Double gradeAProduction = productionRepository.getTotalProductionByGrade(QualityGrade.GRADE_A);
        if (gradeAProduction == null) gradeAProduction = 0.0;

        // This month deliveries
        Double thisMonthTotal = productionRepository.getTotalProductionInPeriod(firstDayOfMonth, lastDayOfMonth);
        if (thisMonthTotal == null) thisMonthTotal = 0.0;

        // Last month deliveries
        Double lastMonthTotal = productionRepository.getTotalProductionInPeriod(lastMonthStart, lastMonthEnd);
        if (lastMonthTotal == null) lastMonthTotal = 0.0;

        // Active farmers
        Long activeFarmers = productionRepository.countActiveFarmers(firstDayOfMonth, lastDayOfMonth);
        Long totalFarmers = farmerRepository.count();

        // Calculate percentages
        double gradeAPercent = totalProduction > 0 ? (gradeAProduction / totalProduction * 100) : 0;
        double monthChange = lastMonthTotal > 0 ? ((thisMonthTotal - lastMonthTotal) / lastMonthTotal * 100) : 0;
        double participationRate = totalFarmers > 0 ? ((double) activeFarmers / totalFarmers * 100) : 0;

        // Get distributions
        Map<String, Double> cropDist = getCropDistributionMap();
        Map<String, Long> statusDist = getStatusDistributionMap();
        Map<String, Double> gradeDist = getGradeDistributionMap();
        Map<String, Double> warehouseDist = getWarehouseDistributionMap();

        return ProductionDashboardMetrics.builder()
                .totalProduction(String.format("%.1f MT", totalProduction))
                .totalProductionPercent(String.format("+%.1f%% vs last cycle", 15.2)) // Mock for now
                .activeFarmers(activeFarmers.intValue())
                .activeFarmersParticipation(String.format("%.1f%% participation rate", participationRate))
                .gradeAProduction(String.format("%.1f MT", gradeAProduction))
                .gradeAPercent(String.format("%.1f%% premium quality", gradeAPercent))
                .thisMonthDeliveries(String.format("%.1f MT", thisMonthTotal))
                .thisMonthChange(String.format("%+.1f%% vs last month", monthChange))
                .cropDistribution(cropDist)
                .statusDistribution(statusDist)
                .gradeDistribution(gradeDist)
                .warehouseDistribution(warehouseDist)
                .build();
    }

    private Map<String, Double> getCropDistributionMap() {
        List<Object[]> results = productionRepository.getCropDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> ((CropType) arr[0]).name(),
                        arr -> (Double) arr[1]
                ));
    }

    private Map<String, Long> getStatusDistributionMap() {
        List<Object[]> results = productionRepository.getStatusDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> ((ProductionStatus) arr[0]).name(),
                        arr -> (Long) arr[1]
                ));
    }

    private Map<String, Double> getGradeDistributionMap() {
        List<Object[]> results = productionRepository.getGradeDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> ((QualityGrade) arr[0]).name(),
                        arr -> (Double) arr[1]
                ));
    }

    private Map<String, Double> getWarehouseDistributionMap() {
        List<Object[]> results = productionRepository.getWarehouseDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Double) arr[1]
                ));
    }

    private Specification<ProductionRecord> buildSpecification(ProductionFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (filter.getCropType() != null) {
                predicates.add(cb.equal(root.get("cropType"), filter.getCropType()));
            }
            if (filter.getQualityGrade() != null) {
                predicates.add(cb.equal(root.get("qualityGrade"), filter.getQualityGrade()));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getWarehouse() != null) {
                predicates.add(cb.equal(root.get("warehouse"), filter.getWarehouse()));
            }
            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("deliveryDate"), filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("deliveryDate"), filter.getEndDate()));
            }
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("productionId")), searchPattern),
                        cb.like(cb.lower(root.get("farmer").get("name")), searchPattern)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private ProductionResponse toResponse(ProductionRecord production) {
        ProductionResponse response = new ProductionResponse();
        response.setProductionId(production.getProductionId());
        response.setDeliveryDate(production.getDeliveryDate());
        response.setFarmerId(production.getFarmer().getUserId());
        response.setFarmerName(production.getFarmer().getName());
        response.setCropType(production.getCropType());
        response.setQuantity(production.getQuantity());
        response.setQualityGrade(production.getQualityGrade());
        response.setWarehouse(production.getWarehouse());
        response.setValueXaf(production.getValueXaf());
        response.setStatus(production.getStatus());
        response.setNotes(production.getNotes());
        response.setCreatedAt(production.getCreatedAt());
        response.setVerifiedBy(production.getVerifiedBy());
        response.setVerificationDate(production.getVerificationDate());
        return response;
    }

    private String generateProductionId() {
        return "PROD" + String.format("%05d", System.currentTimeMillis() % 100000);
    }
}