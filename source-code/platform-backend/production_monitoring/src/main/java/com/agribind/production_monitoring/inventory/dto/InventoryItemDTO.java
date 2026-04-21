package com.agribind.production_monitoring.inventory.dto;

import java.math.BigDecimal;

public class InventoryItemDTO {
    private String id;
    private String itemId;
    private String itemName;
    private String category;
    private Double quantity;
    private Double minimumQuantity;
    private String unit;
    private BigDecimal valueXAF;
    private String location;
    private String status;
    private String trend;
    private String itemType;

    public InventoryItemDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getMinimumQuantity() { return minimumQuantity; }
    public void setMinimumQuantity(Double minimumQuantity) { this.minimumQuantity = minimumQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getValueXAF() { return valueXAF; }
    public void setValueXAF(BigDecimal valueXAF) { this.valueXAF = valueXAF; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
}
