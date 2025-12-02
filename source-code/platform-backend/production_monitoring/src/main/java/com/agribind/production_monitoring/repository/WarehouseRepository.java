package com.agribind.production_monitoring.repository;

import com.agribind.production_monitoring.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByCooperativeId(Long cooperativeId);

    List<Warehouse> findByCooperativeIdAndIsActive(Long cooperativeId, Boolean isActive);

    List<Warehouse> findByLocationContainingIgnoreCase(String location);
}