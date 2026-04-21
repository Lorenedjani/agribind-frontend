package com.agribind.production_monitoring.inventory.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "itemType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FarmerProduct.class, name = "FARMER_PRODUCT"),
        @JsonSubTypes.Type(value = InputSupply.class, name = "INPUT_SUPPLY"),
        @JsonSubTypes.Type(value = Livestock.class, name = "LIVESTOCK")
})
public abstract class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    @Column(unique = true)
    private String itemId;

    @NotBlank
    private String itemName;

    @NotBlank
    private String category;

    @NotNull
    private Double quantity;

    @NotNull
    private Double minimumQuantity;

    private String unit;

    @NotNull
    private BigDecimal valueXAF;

    @NotBlank
    private String location;

    @Enumerated(EnumType.STRING)
    private StockStatus status;

    @Enumerated(EnumType.STRING)
    private Trend trend;

    public enum StockStatus {
        IN_STOCK, LOW_STOCK, CRITICAL, OUT_OF_STOCK
    }

    public enum Trend {
        UP, DOWN, STABLE
    }

    public InventoryItem() {}

    public InventoryItem(String itemId, String itemName, String category, Double quantity,
                        Double minimumQuantity, String unit, BigDecimal valueXAF,
                        String location, StockStatus status, Trend trend) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.minimumQuantity = minimumQuantity;
        this.unit = unit;
        this.valueXAF = valueXAF;
        this.location = location;
        this.status = status;
        this.trend = trend;
    }

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
    public StockStatus getStatus() { return status; }
    public void setStatus(StockStatus status) { this.status = status; }
    public Trend getTrend() { return trend; }
    public void setTrend(Trend trend) { this.trend = trend; }
}
