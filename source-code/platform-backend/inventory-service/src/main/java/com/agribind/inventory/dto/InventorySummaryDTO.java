package com.agribind.inventory.dto;

import java.math.BigDecimal;

public class InventorySummaryDTO {
    private BigDecimal totalInventoryValue;
    private Double farmerProductsStock;
    private Long inputSuppliesCount;
    private Long criticalStockItems;
    private Double percentageChange;

    // Constructors, Getters, Setters
    public InventorySummaryDTO() {}

    public InventorySummaryDTO(BigDecimal totalInventoryValue, Double farmerProductsStock,
                              Long inputSuppliesCount, Long criticalStockItems, Double percentageChange) {
        this.totalInventoryValue = totalInventoryValue;
        this.farmerProductsStock = farmerProductsStock;
        this.inputSuppliesCount = inputSuppliesCount;
        this.criticalStockItems = criticalStockItems;
        this.percentageChange = percentageChange;
    }

    // ... getters and setters
}