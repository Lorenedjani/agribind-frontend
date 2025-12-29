package com.agribind.production_monitoring.dto;

import com.agribind.production_monitoring.model.MaturityStatus;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class MaturityUpdateDTO {

    @NotNull(message = "Production record ID is required")
    private Long productionRecordId;

    @NotNull(message = "New maturity status is required")
    private MaturityStatus newStatus;

    // VERIFIED: Using String for farmer ID to match entity
    @NotNull(message = "Farmer ID is required")
    private String farmerId;

    @Size(max = 500)
    private String notes;
}