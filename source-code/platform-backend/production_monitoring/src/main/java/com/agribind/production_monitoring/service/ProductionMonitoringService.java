package com.agribind.production_monitoring.service;

import com.agribind.production_monitoring.model.*;
import com.agribind.production_monitoring.dto.*;
import com.agribind.production_monitoring.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
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

        // Update farmer contribution
        updateFarmerContribution(savedRecord);

        // Update aggregate statistics
        updateProductionAggregates(savedRecord);

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
            Long cooperativeId,
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
    public ProductionDashboardDTO getProductionDashboard(Long cooperativeId, LocalDate startDate, LocalDate endDate) {
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
                .cooperativeId(cooperativeId)
                .productionByType(aggregates)
                .reportPeriod(startDate + " to " + endDate)
                .build();
    }

    /**
     * Get farmer's production history
     */
    public List<ProductionRecord> getFarmerProductionHistory(Long farmerId) {
        return productionRecordRepository.findByFarmerId(farmerId);
    }

    /**
     * Get products by maturity status
     */
    public List<ProductionRecord> getProductsByMaturityStatus(
            Long cooperativeId,
            String productName,
            MaturityStatus status
    ) {
        return productionRecordRepository.findByCooperativeIdAndProductNameAndMaturityStatus(
                cooperativeId, productName, status
        );
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

            // Recalculate total farmers
            Integer farmerCount = productionRecordRepository.countDistinctFarmersByProduct(
                    record.getCooperativeId(), record.getProductName(), periodStart, periodEnd
            );
            aggregate.setTotalFarmers(farmerCount != null ? farmerCount : 0);

            productionAggregateRepository.save(aggregate);
        } else {
            ProductionAggregate newAggregate = new ProductionAggregate();
            newAggregate.setCooperativeId(record.getCooperativeId());
            newAggregate.setProductName(record.getProductName());
            newAggregate.setProductType(record.getProductType());
            newAggregate.setTotalQuantity(record.getQuantity());
            newAggregate.setTotalFarmers(1);
            newAggregate.setPeriodStart(periodStart);
            newAggregate.setPeriodEnd(periodEnd);
            productionAggregateRepository.save(newAggregate);
        }
    }
}