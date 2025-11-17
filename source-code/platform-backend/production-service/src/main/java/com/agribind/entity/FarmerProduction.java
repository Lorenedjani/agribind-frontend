package com.agribind.entity;

//import jakarta.persistence.*;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="farmer_production")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FarmerProduction {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name="farmer_id", nullable=false)
    private String farmerId;

    @Column(name="cooperative_id")
    private String cooperativeId;

    private String cropType;
    private double quantity;
    private String unit;
    private LocalDate harvestDate;
}

