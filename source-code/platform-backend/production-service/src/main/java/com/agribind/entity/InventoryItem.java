package com.agribind.entity;

//import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

//@Entity
@Table(name="inventory_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryItem {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name="cooperative_id", nullable=false)
    private String cooperativeId;

    private String name;
    private String type;
    private double quantity;
    private String unit;
    private double lowStockThreshold;
    private boolean isLowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }

    public boolean checkAndMarkLow() {
        this.isLowStock = this.quantity < this.lowStockThreshold;
        return this.isLowStock;
    }
}
