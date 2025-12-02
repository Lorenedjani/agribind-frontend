package com.agribind.production_monitoring.dto;

import com.agribind.production_monitoring.model.MaturityStatus;
import com.agribind.production_monitoring.model.ProductType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class ProductionRecordDTO {

    // Getters and setters
    private Long id;

    @NotNull(message = "Farmer ID is required")
    private Long farmerId;

    @NotNull(message = "Cooperative ID is required")
    private Long cooperativeId;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotBlank(message = "Product name is required")
    @Size(max = 100)
    private String productName;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    private String qualityGrade;

    @NotNull(message = "Maturity status is required")
    private MaturityStatus maturityStatus;

    @NotNull(message = "Production date is required")
    private LocalDate productionDate;

    private LocalDate harvestDate;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double locationLatitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double locationLongitude;

    @Size(max = 1000)
    private String notes;

    // NEW: Price fields
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @NotNull(message = "Total value is required")
    @DecimalMin(value = "0.01", message = "Total value must be greater than 0")
    private BigDecimal valueXaf;

}