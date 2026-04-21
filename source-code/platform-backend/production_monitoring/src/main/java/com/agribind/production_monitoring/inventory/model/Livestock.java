package com.agribind.production_monitoring.inventory.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("LIVESTOCK")
public class Livestock extends InventoryItem {
    private String breed;
    private String healthStatus;

    public Livestock() {}

    public Livestock(String itemId, String itemName, String category, Double quantity,
                    Double minimumQuantity, String unit, BigDecimal valueXAF,
                    String location, StockStatus status, Trend trend, String breed) {
        super(itemId, itemName, category, quantity, minimumQuantity, unit, valueXAF, location, status, trend);
        this.breed = breed;
    }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
}
