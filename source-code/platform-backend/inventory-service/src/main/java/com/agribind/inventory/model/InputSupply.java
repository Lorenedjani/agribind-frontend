package com.agribind.inventory.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("INPUT_SUPPLY")
public class InputSupply extends InventoryItem {
    private String supplier;
    private String batchNumber;

    public InputSupply() {}

    public InputSupply(String itemId, String itemName, String category, Double quantity,
                      Double minimumQuantity, String unit, BigDecimal valueXAF,
                      String location, StockStatus status, Trend trend, String supplier) {
        super(itemId, itemName, category, quantity, minimumQuantity, unit, valueXAF, location, status, trend);
        this.supplier = supplier;
    }

    // Getters and Setters
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
}