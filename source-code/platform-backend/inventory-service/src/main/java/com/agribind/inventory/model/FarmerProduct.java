package com.agribind.inventory.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("FARMER_PRODUCT")
public class FarmerProduct extends InventoryItem {
    private String grade;
    private String quality;

    public FarmerProduct() {}

    public FarmerProduct(String itemId, String itemName, String category, Double quantity,
                        Double minimumQuantity, String unit, BigDecimal valueXAF,
                        String location, StockStatus status, Trend trend, String grade) {
        super(itemId, itemName, category, quantity, minimumQuantity, unit, valueXAF, location, status, trend);
        this.grade = grade;
    }

    // Getters and Setters
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
}