package cm.agribind.usermanagement.dto;

import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.QualityGrade;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProductionRequest {
    @NotNull(message = "Farmer ID is required")
    private String farmerId;

    @NotNull(message = "Crop type is required")
    private CropType cropType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantity;

    @NotNull(message = "Quality grade is required")
    private QualityGrade qualityGrade;

    @NotNull(message = "Warehouse is required")
    private String warehouse;

    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;

    private String notes;
}