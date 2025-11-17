package cm.agribind.usermanagement.dto;

import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.ProductionStatus;
import cm.agribind.usermanagement.enums.QualityGrade;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductionFilterRequest {
    private CropType cropType;
    private QualityGrade qualityGrade;
    private ProductionStatus status;
    private String farmerId;
    private String warehouse;
    private LocalDate startDate;
    private LocalDate endDate;
    private String searchTerm;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "deliveryDate";
    private String sortDirection = "DESC";
}
