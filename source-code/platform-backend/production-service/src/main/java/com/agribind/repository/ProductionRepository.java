package com.agribind.repository;

import com.agribind.entity.FarmerProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionRepository extends JpaRepository<FarmerProduction, String> {
    List<FarmerProduction> findByCooperativeId(String cooperativeId);
    List<FarmerProduction> findByFarmerId(String farmerId);
}

