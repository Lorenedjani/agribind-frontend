package com.agribind.inventory.dto;

import java.math.BigDecimal;

public class InventorySummaryDTO {
    private BigDecimal totalInventoryValue;
    private Double farmerProductsStock;
    private Long inputSuppliesCount;
    private Long criticalStockItems;
    private Long lowStockItems;
    private Long outOfStockItems;
    private Long totalItems;
    private Double percentageChange;

    // Constructors
    public InventorySummaryDTO() {}

    public InventorySummaryDTO(BigDecimal totalInventoryValue, Double farmerProductsStock,
                               Long inputSuppliesCount, Long criticalStockItems,
                               Long lowStockItems, Long outOfStockItems, Long totalItems,
                               Double percentageChange) {
        this.totalInventoryValue = totalInventoryValue;
        this.farmerProductsStock = farmerProductsStock;
        this.inputSuppliesCount = inputSuppliesCount;
        this.criticalStockItems = criticalStockItems;
        this.lowStockItems = lowStockItems;
        this.outOfStockItems = outOfStockItems;
        this.totalItems = totalItems;
        this.percentageChange = percentageChange;
    }

    // Getters and Setters
    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public void setTotalInventoryValue(BigDecimal totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }

    public Double getFarmerProductsStock() {
        return farmerProductsStock;
    }

    public void setFarmerProductsStock(Double farmerProductsStock) {
        this.farmerProductsStock = farmerProductsStock;
    }

    public Long getInputSuppliesCount() {
        return inputSuppliesCount;
    }

    public void setInputSuppliesCount(Long inputSuppliesCount) {
        this.inputSuppliesCount = inputSuppliesCount;
    }

    public Long getCriticalStockItems() {
        return criticalStockItems;
    }

    public void setCriticalStockItems(Long criticalStockItems) {
        this.criticalStockItems = criticalStockItems;
    }

    public Double getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(Double percentageChange) {
        this.percentageChange = percentageChange;
    }

    public Long getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(Long lowStockItems) {
        this.lowStockItems = lowStockItems;
    }

    public Long getOutOfStockItems() {
        return outOfStockItems;
    }

    public void setOutOfStockItems(Long outOfStockItems) {
        this.outOfStockItems = outOfStockItems;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }
}