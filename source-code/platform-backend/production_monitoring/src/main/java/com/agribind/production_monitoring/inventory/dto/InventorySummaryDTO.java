package com.agribind.production_monitoring.inventory.dto;

import java.math.BigDecimal;

public class InventorySummaryDTO {
    private BigDecimal totalInventoryValue;
    private Double farmerProductsStock;
    private Long inputSuppliesCount;
    private Long criticalStockItems;
    private Double percentageChange;

    public InventorySummaryDTO() {}

    public InventorySummaryDTO(BigDecimal totalInventoryValue, Double farmerProductsStock,
                              Long inputSuppliesCount, Long criticalStockItems, Double percentageChange) {
        this.totalInventoryValue = totalInventoryValue;
        this.farmerProductsStock = farmerProductsStock;
        this.inputSuppliesCount = inputSuppliesCount;
        this.criticalStockItems = criticalStockItems;
        this.percentageChange = percentageChange;
    }

    public BigDecimal getTotalInventoryValue() { return totalInventoryValue; }
    public void setTotalInventoryValue(BigDecimal totalInventoryValue) { this.totalInventoryValue = totalInventoryValue; }
    public Double getFarmerProductsStock() { return farmerProductsStock; }
    public void setFarmerProductsStock(Double farmerProductsStock) { this.farmerProductsStock = farmerProductsStock; }
    public Long getInputSuppliesCount() { return inputSuppliesCount; }
    public void setInputSuppliesCount(Long inputSuppliesCount) { this.inputSuppliesCount = inputSuppliesCount; }
    public Long getCriticalStockItems() { return criticalStockItems; }
    public void setCriticalStockItems(Long criticalStockItems) { this.criticalStockItems = criticalStockItems; }
    public Double getPercentageChange() { return percentageChange; }
    public void setPercentageChange(Double percentageChange) { this.percentageChange = percentageChange; }
}
