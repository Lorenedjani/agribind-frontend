package com.agribind.production_monitoring.service;

import com.agribind.production_monitoring.config.UserManagementClient;
import com.agribind.production_monitoring.model.*;
import com.agribind.production_monitoring.dto.*;
import com.agribind.production_monitoring.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionMonitoringService {

    private final ProductionRecordRepository productionRecordRepository;
    private final ProductionAggregateRepository productionAggregateRepository;
    private final FarmerContributionRepository farmerContributionRepository;
    private final MaturityUpdateRepository maturityUpdateRepository;
    private final UserManagementClient userManagementClient;

    /**
     * Record new production entry from a farmer
     */
    @Transactional
    public ProductionRecord recordProduction(ProductionRecordDTO dto) {
        log.info("Recording production for farmer: {}, product: {}", dto.getFarmerId(), dto.getProductName());

        // Create production record
        ProductionRecord record = new ProductionRecord();
        record.setFarmerId(dto.getFarmerId());
        record.setCooperativeId(dto.getCooperativeId());
        record.setProductType(dto.getProductType());
        record.setProductName(dto.getProductName());
        record.setQuantity(dto.getQuantity());
        record.setUnit(dto.getUnit());
        record.setQualityGrade(dto.getQualityGrade());
        record.setMaturityStatus(dto.getMaturityStatus());
        record.setProductionDate(dto.getProductionDate());
        record.setHarvestDate(dto.getHarvestDate());
        record.setLocationLatitude(dto.getLocationLatitude());
        record.setLocationLongitude(dto.getLocationLongitude());
        record.setNotes(dto.getNotes());

        // Set farmer details (either from DTO or fetch from UserManagement)
        if (dto.getFarmerRegion() != null && !dto.getFarmerRegion().isEmpty() &&
            dto.getFarmerName() != null && !dto.getFarmerName().isEmpty()) {
            record.setFarmerRegion(dto.getFarmerRegion());
            record.setFarmerName(dto.getFarmerName());
        } else {
            UserManagementClient.FarmerInfo info = userManagementClient.getFarmerInfo(dto.getFarmerId());
            record.setFarmerRegion(info.region() != null ? info.region() : "Unknown");
            record.setFarmerName(info.name() != null ? info.name() : "Farmer " + dto.getFarmerId());
        }

        // Set price information
        record.setUnitPrice(dto.getUnitPrice());
        record.setValueXaf(dto.getValueXaf());

        // Validate price calculation
        BigDecimal calculatedValue = dto.getUnitPrice().multiply(dto.getQuantity());
        if (calculatedValue.compareTo(dto.getValueXaf()) != 0) {
            log.warn("Price mismatch detected. Calculated: {}, Provided: {}",
                    calculatedValue, dto.getValueXaf());
            // Use calculated value to ensure accuracy
            record.setValueXaf(calculatedValue);
        }

        ProductionRecord savedRecord = productionRecordRepository.save(record);

        // TODO: Update farmer contribution (needs FarmerContribution entity update to String IDs)
        // updateFarmerContribution(savedRecord);

        // TODO: Update aggregate statistics (needs ProductionAggregate entity update to String IDs)
        // updateProductionAggregates(savedRecord);

        log.info("Production recorded successfully with ID: {}, Value: {} XAF",
                savedRecord.getId(), savedRecord.getValueXaf());
        return savedRecord;
    }

    /**
     * Update maturity status of production
     */
    @Transactional
    public ProductionRecord updateMaturityStatus(MaturityUpdateDTO dto) {
        log.info("Updating maturity status for production record: {}", dto.getProductionRecordId());

        ProductionRecord record = productionRecordRepository.findById(dto.getProductionRecordId())
                .orElseThrow(() -> new RuntimeException("Production record not found"));

        // Create maturity update history
        MaturityUpdate maturityUpdate = new MaturityUpdate();
        maturityUpdate.setProductionRecordId(dto.getProductionRecordId());
        maturityUpdate.setPreviousStatus(record.getMaturityStatus().name());
        maturityUpdate.setNewStatus(dto.getNewStatus().name());
        maturityUpdate.setUpdatedByFarmerId(dto.getFarmerId());
        maturityUpdate.setNotes(dto.getNotes());

        maturityUpdateRepository.save(maturityUpdate);

        // Update production record
        record.setMaturityStatus(dto.getNewStatus());

        // If harvested, set harvest date
        if (dto.getNewStatus() == MaturityStatus.HARVESTED && record.getHarvestDate() == null) {
            record.setHarvestDate(LocalDate.now());
        }

        return productionRecordRepository.save(record);
    }

    /**
     * Get production aggregate for a specific product
     */
    public ProductionAggregateDTO getProductionAggregate(
            String cooperativeId,
            String productName,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("Getting production aggregate for cooperative: {}, product: {}", cooperativeId, productName);

        // Get production records for the period
        List<ProductionRecord> records = productionRecordRepository.findByCooperativeIdAndDateRange(
                        cooperativeId, startDate, endDate
                ).stream()
                .filter(r -> r.getProductName().equals(productName))
                .collect(Collectors.toList());

        // Calculate total quantity and value
        BigDecimal totalQuantity = records.stream()
                .map(ProductionRecord::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = records.stream()
                .map(ProductionRecord::getValueXaf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count unique farmers
        Integer totalFarmers = (int) records.stream()
                .map(ProductionRecord::getFarmerId)
                .distinct()
                .count();

        // Get top contributors
        List<FarmerContribution> contributions = farmerContributionRepository.findTopContributors(
                cooperativeId, productName, PageRequest.of(0, 10)
        );

        List<FarmerContributionSummary> topContributors = contributions.stream()
                .map(fc -> {
                    Double percentage = totalQuantity.compareTo(BigDecimal.ZERO) > 0
                            ? fc.getQuantityContributed().divide(totalQuantity, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return FarmerContributionSummary.builder()
                            .farmerId(fc.getFarmerId())
                            .farmerName(fc.getFarmerName())
                            .quantityContributed(fc.getQuantityContributed())
                            .contributionCount(fc.getContributionCount())
                            .percentageOfTotal(percentage)
                            .build();
                })
                .collect(Collectors.toList());

        return ProductionAggregateDTO.builder()
                .cooperativeId(cooperativeId)
                .productName(productName)
                .totalQuantity(totalQuantity)
                .totalFarmers(totalFarmers)
                .topContributors(topContributors)
                .periodDescription(startDate + " to " + endDate)
                .build();
    }

    /**
     * Get production dashboard for cooperative
     */
    public ProductionDashboardDTO getProductionDashboard(String cooperativeId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating production dashboard for cooperative: {}", cooperativeId);

        List<String> cropProducts = productionRecordRepository.findDistinctProductNamesByCooperativeIdAndProductType(
                cooperativeId, ProductType.CROP
        );

        List<String> livestockProducts = productionRecordRepository.findDistinctProductNamesByCooperativeIdAndProductType(
                cooperativeId, ProductType.LIVESTOCK
        );

        List<ProductionAggregateDTO> aggregates = cropProducts.stream()
                .map(product -> getProductionAggregate(cooperativeId, product, startDate, endDate))
                .collect(Collectors.toList());

        aggregates.addAll(livestockProducts.stream()
                .map(product -> getProductionAggregate(cooperativeId, product, startDate, endDate))
                .collect(Collectors.toList()));

        return ProductionDashboardDTO.builder()
                .cooperativeId(Long.valueOf(cooperativeId))
                .productionByType(aggregates)
                .reportPeriod(startDate + " to " + endDate)
                .build();
    }

    /**
     * Get analytics: Crops grouped by Region
     */
    /** readOnly=false: MySQL stored procedures are not accepted on read-only JDBC connections. */
    @Transactional(readOnly = false)
    public List<CropsByRegionDTO> getCropsByRegion(String cooperativeId, LocalDate startDate, LocalDate endDate) {
        // Execute Stored Procedure
        List<Object[]> rawResults = productionRecordRepository.getCropsByRegionStoredProcedure(
                cooperativeId, startDate, endDate
        );

        // Map results
        Map<String, CropsByRegionDTO> regionMap = new java.util.LinkedHashMap<>();

        for (Object[] row : rawResults) {
            String region = row[0] != null ? row[0].toString() : "Unknown";
            String cropName = row[1] != null ? row[1].toString() : "Unknown";
            BigDecimal quantity = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            String unit = row[3] != null ? row[3].toString() : "-";
            Integer farmerCount = row[4] != null ? Integer.valueOf(row[4].toString()) : 0;

            CropsByRegionDTO dto = regionMap.computeIfAbsent(region, r -> 
                CropsByRegionDTO.builder()
                        .region(region)
                        .totalQuantity(BigDecimal.ZERO)
                        .farmerCount(0)
                        .cropTypeCount(0)
                        .crops(new java.util.ArrayList<>())
                        .build()
            );

            // Accumulate totals
            dto.setTotalQuantity(dto.getTotalQuantity().add(quantity));
            dto.setFarmerCount(dto.getFarmerCount() + farmerCount); // Approximate total
            dto.setCropTypeCount(dto.getCropTypeCount() + 1);

            // Add crop entry
            dto.getCrops().add(CropsByRegionDTO.CropEntry.builder()
                    .cropName(cropName)
                    .quantity(quantity)
                    .unit(unit)
                    .farmerCount(farmerCount)
                    .build());
        }

        return new java.util.ArrayList<>(regionMap.values());
    }

    /**
     * Get analytics: Crop production per farmer
     */
    @Transactional(readOnly = false)
    public List<CropsByFarmerDTO> getCropsByFarmer(String cooperativeId, String region, LocalDate startDate, LocalDate endDate) {
        if ("All Regions".equalsIgnoreCase(region) || "all".equalsIgnoreCase(region)) {
            region = null;
        }

        // Execute Stored Procedure
        List<Object[]> rawResults = productionRecordRepository.getCropsByFarmerStoredProcedure(
                cooperativeId, region, startDate, endDate
        );

        // Map Results
        Map<String, CropsByFarmerDTO> farmerMap = new java.util.LinkedHashMap<>();

        for (Object[] row : rawResults) {
            String farmerId = row[0] != null ? row[0].toString() : "Unknown";
            String farmerName = row[1] != null ? row[1].toString() : "Unknown";
            String farmerRegion = row[2] != null ? row[2].toString() : "Unknown";
            String cropName = row[3] != null ? row[3].toString() : "Unknown";
            BigDecimal cropQuantity = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;
            BigDecimal cropValue = row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO;
            String qualityGrade = row[6] != null ? row[6].toString() : "N/A";
            Integer deliveryCount = row[7] != null ? Integer.valueOf(row[7].toString()) : 0;

            CropsByFarmerDTO dto = farmerMap.computeIfAbsent(farmerId, fId -> 
                CropsByFarmerDTO.builder()
                        .farmerId(fId)
                        .farmerName(farmerName)
                        .farmerRegion(farmerRegion)
                        .totalQuantity(BigDecimal.ZERO)
                        .totalValueXaf(BigDecimal.ZERO)
                        .deliveryCount(0)
                        .crops(new java.util.ArrayList<>())
                        .build()
            );

            // Accumulate totals
            dto.setTotalQuantity(dto.getTotalQuantity().add(cropQuantity));
            dto.setTotalValueXaf(dto.getTotalValueXaf().add(cropValue));
            dto.setDeliveryCount(dto.getDeliveryCount() + deliveryCount);

            // Add crop entry
            dto.getCrops().add(CropsByFarmerDTO.CropEntry.builder()
                    .cropName(cropName)
                    .quantity(cropQuantity)
                    .qualityGrade(qualityGrade)
                    .build());
        }

        return new java.util.ArrayList<>(farmerMap.values());
    }

    /**
     * Get farmer's production history
     */
    public List<ProductionRecord> getFarmerProductionHistory(String farmerId) {
        return productionRecordRepository.findByFarmerId(farmerId);
    }

    /**
     * Get products by maturity status
     */
    public List<ProductionRecord> getProductsByMaturityStatus(
            String cooperativeId,
            String productName,
            MaturityStatus status
    ) {
        return productionRecordRepository.findByCooperativeIdAndProductNameAndMaturityStatus(
                cooperativeId, productName, status
        );
    }

    /**
     * Get all production records for a cooperative with pagination and filters
     */
    public Page<ProductionRecord> getProductionRecords(
            String cooperativeId,
            int page,
            int size,
            String productName,
            String qualityGrade,
            String searchTerm
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "productionDate"));
        
        if (productName != null && !productName.isEmpty() && !productName.equals("All Crops")) {
            return productionRecordRepository.findByCooperativeIdAndProductName(cooperativeId, productName, pageable);
        }
        
        // For now, return all records for the cooperative. In a real scenario, you'd add more filtering
        List<ProductionRecord> allRecords = productionRecordRepository.findByCooperativeId(cooperativeId);
        
        // Apply filters
        List<ProductionRecord> filtered = allRecords.stream()
                .filter(record -> {
                    if (productName != null && !productName.isEmpty() && !productName.equals("All Crops")) {
                        if (!record.getProductName().equals(productName)) return false;
                    }
                    if (qualityGrade != null && !qualityGrade.isEmpty() && !qualityGrade.equals("All Grades")) {
                        if (record.getQualityGrade() == null || !record.getQualityGrade().equals(qualityGrade)) return false;
                    }
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        String search = searchTerm.toLowerCase();
                        // Search in notes, product name, etc.
                        boolean matches = (record.getNotes() != null && record.getNotes().toLowerCase().contains(search)) ||
                                record.getProductName().toLowerCase().contains(search) ||
                                record.getId().toString().contains(search);
                        if (!matches) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<ProductionRecord> paginated = start < filtered.size() ? filtered.subList(start, end) : List.of();
        
        return new PageImpl<>(paginated, pageable, filtered.size());
    }

    /**
     * Get production record by ID
     */
    public ProductionRecord getProductionRecordById(Long id) {
        return productionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production record not found with ID: " + id));
    }

    /**
     * Update production record
     */
    @Transactional
    public ProductionRecord updateProductionRecord(Long id, ProductionRecordDTO dto) {
        log.info("Updating production record with ID: {}", id);
        
        ProductionRecord record = productionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production record not found with ID: " + id));
        
        // Update fields
        if (dto.getFarmerId() != null) record.setFarmerId(dto.getFarmerId());
        if (dto.getProductType() != null) record.setProductType(dto.getProductType());
        if (dto.getProductName() != null) record.setProductName(dto.getProductName());
        if (dto.getQuantity() != null) record.setQuantity(dto.getQuantity());
        if (dto.getUnit() != null) record.setUnit(dto.getUnit());
        if (dto.getQualityGrade() != null) record.setQualityGrade(dto.getQualityGrade());
        if (dto.getMaturityStatus() != null) record.setMaturityStatus(dto.getMaturityStatus());
        if (dto.getProductionDate() != null) record.setProductionDate(dto.getProductionDate());
        if (dto.getHarvestDate() != null) record.setHarvestDate(dto.getHarvestDate());
        if (dto.getLocationLatitude() != null) record.setLocationLatitude(dto.getLocationLatitude());
        if (dto.getLocationLongitude() != null) record.setLocationLongitude(dto.getLocationLongitude());
        if (dto.getNotes() != null) record.setNotes(dto.getNotes());
        if (dto.getUnitPrice() != null) record.setUnitPrice(dto.getUnitPrice());
        if (dto.getValueXaf() != null) record.setValueXaf(dto.getValueXaf());
        
        // Recalculate value if quantity or unit price changed
        if (dto.getQuantity() != null || dto.getUnitPrice() != null) {
            BigDecimal calculatedValue = record.getUnitPrice().multiply(record.getQuantity());
            record.setValueXaf(calculatedValue);
        }
        
        return productionRecordRepository.save(record);
    }

    /**
     * Delete production record
     */
    @Transactional
    public void deleteProductionRecord(Long id) {
        log.info("Deleting production record with ID: {}", id);
        
        ProductionRecord record = productionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production record not found with ID: " + id));
        
        productionRecordRepository.delete(record);
        log.info("Production record deleted successfully with ID: {}", id);
    }

    // Private helper methods

    private void updateFarmerContribution(ProductionRecord record) {
        Optional<FarmerContribution> existingContribution =
                farmerContributionRepository.findByFarmerIdAndCooperativeIdAndProductNameAndProductType(
                        record.getFarmerId(),
                        record.getCooperativeId(),
                        record.getProductName(),
                        record.getProductType()
                );

        if (existingContribution.isPresent()) {
            FarmerContribution contribution = existingContribution.get();
            contribution.setQuantityContributed(
                    contribution.getQuantityContributed().add(record.getQuantity())
            );
            contribution.setLastContributionDate(record.getProductionDate());
            contribution.setContributionCount(contribution.getContributionCount() + 1);
            farmerContributionRepository.save(contribution);
        } else {
            FarmerContribution newContribution = new FarmerContribution();
            newContribution.setFarmerId(record.getFarmerId());
            newContribution.setFarmerName("Farmer " + record.getFarmerId()); // Should fetch from farmer service
            newContribution.setCooperativeId(record.getCooperativeId());
            newContribution.setProductName(record.getProductName());
            newContribution.setProductType(record.getProductType());
            newContribution.setQuantityContributed(record.getQuantity());
            newContribution.setLastContributionDate(record.getProductionDate());
            newContribution.setContributionCount(1);
            farmerContributionRepository.save(newContribution);
        }
    }

    private void updateProductionAggregates(ProductionRecord record) {
        LocalDate periodStart = record.getProductionDate().withDayOfMonth(1);
        LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);

        Optional<ProductionAggregate> existing =
                productionAggregateRepository.findByCooperativeIdAndProductNameAndPeriodStartAndPeriodEnd(
                        record.getCooperativeId(),
                        record.getProductName(),
                        periodStart,
                        periodEnd
                );

        if (existing.isPresent()) {
            ProductionAggregate aggregate = existing.get();
            aggregate.setTotalQuantity(aggregate.getTotalQuantity().add(record.getQuantity()));
            aggregate.setLastUpdated(LocalDateTime.now());
            productionAggregateRepository.save(aggregate);
        } else {
            ProductionAggregate newAggregate = new ProductionAggregate();
            newAggregate.setCooperativeId(record.getCooperativeId());
            newAggregate.setProductName(record.getProductName());
            newAggregate.setProductType(record.getProductType());
            newAggregate.setPeriodStart(periodStart);
            newAggregate.setPeriodEnd(periodEnd);
            newAggregate.setTotalQuantity(record.getQuantity());
            newAggregate.setLastUpdated(LocalDateTime.now());
            productionAggregateRepository.save(newAggregate);
        }
    }
}