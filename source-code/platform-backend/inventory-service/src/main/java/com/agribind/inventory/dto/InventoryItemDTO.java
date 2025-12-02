package com.agribind.inventory.dto;

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

    // Constructors, Getters, Setters
    public InventoryItemDTO() {}

    // ... getters and setters for all fields
}

