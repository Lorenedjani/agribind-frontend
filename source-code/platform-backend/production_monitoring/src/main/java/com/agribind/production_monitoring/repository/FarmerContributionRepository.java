package com.agribind.production_monitoring.repository;

import com.agribind.production_monitoring.model.FarmerContribution;
import com.agribind.production_monitoring.model.ProductType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerContributionRepository extends JpaRepository<FarmerContribution, Long> {

    Optional<FarmerContribution> findByFarmerIdAndCooperativeIdAndProductNameAndProductType(
            String farmerId,
            Long cooperativeId,
            String productName,
            ProductType productType
    );

    List<FarmerContribution> findByCooperativeIdAndProductName(
            Long cooperativeId,
            String productName
    );

    @Query("SELECT fc FROM FarmerContribution fc " +
           "WHERE fc.cooperativeId = :cooperativeId AND fc.productName = :productName " +
           "ORDER BY fc.quantityContributed DESC")
    List<FarmerContribution> findTopContributors(
            @Param("cooperativeId") Long cooperativeId,
            @Param("productName") String productName,
            Pageable pageable
    );

    @Query("SELECT COUNT(DISTINCT fc.farmerId) FROM FarmerContribution fc " +
           "WHERE fc.cooperativeId = :cooperativeId AND fc.productName = :productName")
    Integer countDistinctFarmers(
            @Param("cooperativeId") Long cooperativeId,
            @Param("productName") String productName
    );

    List<FarmerContribution> findByFarmerId(Long farmerId);
}
