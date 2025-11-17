package cm.agribind.usermanagement.dto;

import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.ProductionStatus;
import cm.agribind.usermanagement.enums.QualityGrade;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductionResponse {
    private String productionId;
    private LocalDate deliveryDate;
    private String farmerId;
    private String farmerName;
    private CropType cropType;
    private Double quantity;
    private QualityGrade qualityGrade;
    private String warehouse;
    private Double valueXaf;
    private ProductionStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private String verifiedBy;
    private LocalDate verificationDate;
}
