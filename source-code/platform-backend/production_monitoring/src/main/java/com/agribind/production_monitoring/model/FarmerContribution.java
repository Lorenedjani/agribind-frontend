package com.agribind.production_monitoring.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "farmer_contributions")
public class FarmerContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_id", nullable = false, length = 20)
    private String farmerId;  // Changed from Long to String

    @Column(name = "farmer_name", nullable = false)
    private String farmerName;

    @Column(name = "cooperative_id", nullable = false, length = 20)
    private String cooperativeId;  // Changed from Long to String

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    @Column(name = "quantity_contributed", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityContributed;

    @Column(name = "last_contribution_date", nullable = false)
    private LocalDate lastContributionDate;

    @Column(name = "contribution_count")
    private Integer contributionCount = 1;
}