package com.agribind.production_monitoring.inventory.repository;

import com.agribind.production_monitoring.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, String> {

    Optional<InventoryItem> findByItemId(String itemId);

    List<InventoryItem> findByCategory(String category);

    List<InventoryItem> findByStatus(InventoryItem.StockStatus status);

    List<InventoryItem> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT i FROM InventoryItem i WHERE " +
           "LOWER(i.itemName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.itemId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.category) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<InventoryItem> searchInventory(@Param("search") String search);

    @Query("SELECT SUM(i.valueXAF) FROM InventoryItem i")
    BigDecimal getTotalInventoryValue();

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.status = 'LOW_STOCK' OR i.status = 'CRITICAL'")
    Long countCriticalStockItems();

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE TYPE(i) = com.agribind.production_monitoring.inventory.model.InputSupply")
    Long countInputSupplies();

    @Query("SELECT SUM(i.quantity) FROM com.agribind.production_monitoring.inventory.model.FarmerProduct i")
    Double getTotalFarmerProductsStock();
}
