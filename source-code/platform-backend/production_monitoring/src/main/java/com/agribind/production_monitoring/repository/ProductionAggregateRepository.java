package com.agribind.production_monitoring.repository;

import com.agribind.production_monitoring.model.ProductionAggregate;
import com.agribind.production_monitoring.model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionAggregateRepository extends JpaRepository<ProductionAggregate, Long> {

    List<ProductionAggregate> findByCooperativeId(Long cooperativeId);

    List<ProductionAggregate> findByCooperativeIdAndProductType(
            Long cooperativeId,
            ProductType productType
    );

    Optional<ProductionAggregate> findByCooperativeIdAndProductNameAndPeriodStartAndPeriodEnd(
            Long cooperativeId,
            String productName,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    @Query("SELECT pa FROM ProductionAggregate pa WHERE pa.cooperativeId = :cooperativeId " +
           "AND pa.periodStart >= :startDate AND pa.periodEnd <= :endDate")
    List<ProductionAggregate> findByCooperativeIdAndDateRange(
            @Param("cooperativeId") Long cooperativeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
