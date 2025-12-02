package com.agribind.inventory.service;

import com.agribind.inventory.dto.InventoryItemDTO;
import com.agribind.inventory.dto.InventorySummaryDTO;
import com.agribind.inventory.model.*;
import com.agribind.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<InventoryItem> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Optional<InventoryItem> getInventoryItem(String id) {
        return inventoryRepository.findById(id);
    }

    public Optional<InventoryItem> getInventoryItemByItemId(String itemId) {
        return inventoryRepository.findByItemId(itemId);
    }

    public List<InventoryItem> searchInventory(String searchTerm) {
        return inventoryRepository.searchInventory(searchTerm);
    }

    public List<InventoryItem> getInventoryByCategory(String category) {
        return inventoryRepository.findByCategory(category);
    }

    public List<InventoryItem> getInventoryByStatus(String status) {
        return inventoryRepository.findByStatus(InventoryItem.StockStatus.valueOf(status.toUpperCase()));
    }

    public List<InventoryItem> getInventoryByLocation(String location) {
        return inventoryRepository.findByLocationContainingIgnoreCase(location);
    }

    public InventoryItem createInventoryItem(InventoryItem item) {
        // Auto-calculate status based on quantity
        item.setStatus(calculateStockStatus(item.getQuantity(), item.getMinimumQuantity()));
        return inventoryRepository.save(item);
    }

    public InventoryItem updateInventoryItem(String id, InventoryItem itemDetails) {
        return inventoryRepository.findById(id).map(item -> {
            item.setItemName(itemDetails.getItemName());
            item.setCategory(itemDetails.getCategory());
            item.setQuantity(itemDetails.getQuantity());
            item.setMinimumQuantity(itemDetails.getMinimumQuantity());
            item.setValueXAF(itemDetails.getValueXAF());
            item.setLocation(itemDetails.getLocation());
            item.setStatus(calculateStockStatus(itemDetails.getQuantity(), itemDetails.getMinimumQuantity()));
            item.setTrend(itemDetails.getTrend());
            return inventoryRepository.save(item);
        }).orElse(null);
    }

    public boolean deleteInventoryItem(String id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public InventorySummaryDTO getInventorySummary() {
        BigDecimal totalValue = inventoryRepository.getTotalInventoryValue();
        Double farmerProductsStock = inventoryRepository.getTotalFarmerProductsStock();
        Long inputSuppliesCount = inventoryRepository.countInputSupplies();
        Long criticalItems = inventoryRepository.countCriticalStockItems();

        // Mock percentage change - in real scenario, calculate from historical data
        Double percentageChange = 5.7;

        return new InventorySummaryDTO(
            totalValue != null ? totalValue : BigDecimal.ZERO,
            farmerProductsStock != null ? farmerProductsStock : 0.0,
            inputSuppliesCount != null ? inputSuppliesCount : 0L,
            criticalItems != null ? criticalItems : 0L,
            percentageChange
        );
    }

    private InventoryItem.StockStatus calculateStockStatus(Double quantity, Double minimumQuantity) {
        if (quantity == null || minimumQuantity == null) {
            return InventoryItem.StockStatus.OUT_OF_STOCK;
        }

        if (quantity <= 0) {
            return InventoryItem.StockStatus.OUT_OF_STOCK;
        } else if (quantity <= minimumQuantity * 0.3) {
            return InventoryItem.StockStatus.CRITICAL;
        } else if (quantity <= minimumQuantity * 0.7) {
            return InventoryItem.StockStatus.LOW_STOCK;
        } else {
            return InventoryItem.StockStatus.IN_STOCK;
        }
    }
}