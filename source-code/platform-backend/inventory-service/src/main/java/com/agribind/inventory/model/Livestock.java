package com.agribind.inventory.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("LIVESTOCK")
public class Livestock extends InventoryItem {
    private String breed;
    private String ageGroup;

    public Livestock() {}

    public Livestock(String itemId, String itemName, String category, Double quantity,
                    Double minimumQuantity, String unit, BigDecimal valueXAF,
                    String location, StockStatus status, Trend trend, String breed) {
        super(itemId, itemName, category, quantity, minimumQuantity, unit, valueXAF, location, status, trend);
        this.breed = breed;
    }

    // Getters and Setters
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
}