package cm.agribind.usermanagement.repository;

import cm.agribind.usermanagement.entity.ProductionRecord;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.ProductionStatus;
import cm.agribind.usermanagement.enums.QualityGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionRepository extends JpaRepository<ProductionRecord, Long>,
        JpaSpecificationExecutor<ProductionRecord> {

    Optional<ProductionRecord> findByProductionId(String productionId);

    List<ProductionRecord> findByFarmerId(Long farmerId);

    Page<ProductionRecord> findByFarmerId(Long farmerId, Pageable pageable);

    List<ProductionRecord> findByCropType(CropType cropType);

    List<ProductionRecord> findByQualityGrade(QualityGrade grade);

    List<ProductionRecord> findByStatus(ProductionStatus status);

    List<ProductionRecord> findByWarehouse(String warehouse);

    @Query("SELECT p FROM ProductionRecord p WHERE p.deliveryDate BETWEEN :startDate AND :endDate")
    List<ProductionRecord> findByDeliveryDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(p.quantity) FROM ProductionRecord p WHERE p.status = 'VERIFIED'")
    Double getTotalVerifiedProduction();

    @Query("SELECT SUM(p.quantity) FROM ProductionRecord p WHERE p.qualityGrade = :grade AND p.status = 'VERIFIED'")
    Double getTotalProductionByGrade(@Param("grade") QualityGrade grade);

    @Query("SELECT COUNT(DISTINCT p.farmer.id) FROM ProductionRecord p WHERE p.deliveryDate BETWEEN :startDate AND :endDate")
    Long countActiveFarmers(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(p.quantity) FROM ProductionRecord p WHERE p.deliveryDate BETWEEN :startDate AND :endDate")
    Double getTotalProductionInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT p.cropType, SUM(p.quantity) FROM ProductionRecord p WHERE p.status = 'VERIFIED' GROUP BY p.cropType")
    List<Object[]> getCropDistribution();

    @Query("SELECT p.qualityGrade, SUM(p.quantity) FROM ProductionRecord p GROUP BY p.qualityGrade")
    List<Object[]> getGradeDistribution();

    @Query("SELECT p.warehouse, SUM(p.quantity) FROM ProductionRecord p GROUP BY p.warehouse")
    List<Object[]> getWarehouseDistribution();

    @Query("SELECT p.status, COUNT(p) FROM ProductionRecord p GROUP BY p.status")
    List<Object[]> getStatusDistribution();
}