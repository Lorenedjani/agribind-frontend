package com.agribind.production_monitoring.repository;

import com.agribind.production_monitoring.model.MaturityStatus;
import com.agribind.production_monitoring.model.ProductionRecord;
import com.agribind.production_monitoring.model.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionRecordRepository extends JpaRepository<ProductionRecord, Long> {

    List<ProductionRecord> findByFarmerId(String farmerId);

    List<ProductionRecord> findByCooperativeId(String cooperativeId);

    Page<ProductionRecord> findByCooperativeIdAndProductName(
            String cooperativeId,
            String productName,
            Pageable pageable
    );

    @Query("SELECT pr FROM ProductionRecord pr WHERE pr.cooperativeId = :cooperativeId " +
           "AND pr.productionDate BETWEEN :startDate AND :endDate")
    List<ProductionRecord> findByCooperativeIdAndDateRange(
            @Param("cooperativeId") String cooperativeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT pr FROM ProductionRecord pr WHERE pr.cooperativeId = :cooperativeId " +
           "AND pr.productName = :productName AND pr.maturityStatus = :status")
    List<ProductionRecord> findByCooperativeIdAndProductNameAndMaturityStatus(
            @Param("cooperativeId") String cooperativeId,
            @Param("productName") String productName,
            @Param("status") MaturityStatus status
    );

    @Query("SELECT DISTINCT pr.productName FROM ProductionRecord pr " +
           "WHERE pr.cooperativeId = :cooperativeId AND pr.productType = :productType")
    List<String> findDistinctProductNamesByCooperativeIdAndProductType(
            @Param("cooperativeId") String cooperativeId,
            @Param("productType") ProductType productType
    );

    @Query("SELECT COUNT(DISTINCT pr.farmerId) FROM ProductionRecord pr " +
           "WHERE pr.cooperativeId = :cooperativeId AND pr.productName = :productName " +
           "AND pr.productionDate BETWEEN :startDate AND :endDate")
    Integer countDistinctFarmersByProduct(
            @Param("cooperativeId") String cooperativeId,
            @Param("productName") String productName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(pr.quantity) FROM ProductionRecord pr " +
           "WHERE pr.cooperativeId = :cooperativeId AND pr.productName = :productName " +
           "AND pr.productionDate BETWEEN :startDate AND :endDate")
    Double sumQuantityByProductAndDateRange(
            @Param("cooperativeId") String cooperativeId,
            @Param("productName") String productName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT pr FROM ProductionRecord pr WHERE " +
           "(:cooperativeId IS NULL OR pr.cooperativeId = :cooperativeId) AND " +
           "(:region IS NULL OR pr.farmerRegion = :region) AND " +
           "(pr.productionDate BETWEEN :startDate AND :endDate) " +
           "ORDER BY pr.productionDate DESC")
    List<ProductionRecord> findForReport(
            @Param("cooperativeId") String cooperativeId,
            @Param("region") String region,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Stored procedures for optimized analytics
    @Procedure(procedureName = "GetCropsByRegion")
    List<Object[]> getCropsByRegionStoredProcedure(
            @Param("p_cooperative_id") String cooperativeId,
            @Param("p_start_date") LocalDate startDate,
            @Param("p_end_date") LocalDate endDate
    );

    @Procedure(procedureName = "GetCropsByFarmer")
    List<Object[]> getCropsByFarmerStoredProcedure(
            @Param("p_cooperative_id") String cooperativeId,
            @Param("p_region") String region,
            @Param("p_start_date") LocalDate startDate,
            @Param("p_end_date") LocalDate endDate
    );

    @Procedure(procedureName = "GetFarmerProductionReport")
    List<Object[]> getFarmerProductionReportStoredProcedure(
            @Param("p_cooperative_id") String cooperativeId,
            @Param("p_region") String region,
            @Param("p_start_date") LocalDate startDate,
            @Param("p_end_date") LocalDate endDate
    );
}
