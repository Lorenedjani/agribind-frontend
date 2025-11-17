package com.agribind.repository;

import com.agribind.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
    List<InventoryItem> findByCooperativeId(String cooperativeId);

    List<InventoryItem> findByCooperativeIdAndIsLowStockTrue(String cooperativeId);
}