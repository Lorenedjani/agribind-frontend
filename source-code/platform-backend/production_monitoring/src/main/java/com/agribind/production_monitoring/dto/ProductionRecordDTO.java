package com.agribind.production_monitoring.dto;

import com.agribind.production_monitoring.model.MaturityStatus;
import com.agribind.production_monitoring.model.ProductType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductionRecordDTO {

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

    // Manual getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public Long getCooperativeId() { return cooperativeId; }
    public void setCooperativeId(Long cooperativeId) { this.cooperativeId = cooperativeId; }

    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }

    public MaturityStatus getMaturityStatus() { return maturityStatus; }
    public void setMaturityStatus(MaturityStatus maturityStatus) { this.maturityStatus = maturityStatus; }

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public Double getLocationLatitude() { return locationLatitude; }
    public void setLocationLatitude(Double locationLatitude) { this.locationLatitude = locationLatitude; }

    public Double getLocationLongitude() { return locationLongitude; }
    public void setLocationLongitude(Double locationLongitude) { this.locationLongitude = locationLongitude; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}