package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class ResourceRequestDto {
    @NotBlank
    private String resourceName;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String unit;

    @NotNull
    private UrgencyLevel urgency;

    @NotBlank
    private String requestedBy;

    private String requestedByZone;

    // Default constructor
    public ResourceRequestDto() {}

    // All arguments constructor
    public ResourceRequestDto(String resourceName, Integer quantity, String unit,
                             UrgencyLevel urgency, String requestedBy, String requestedByZone) {
        this.resourceName = resourceName;
        this.quantity = quantity;
        this.unit = unit;
        this.urgency = urgency;
        this.requestedBy = requestedBy;
        this.requestedByZone = requestedByZone;
    }

    // Getters and Setters
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public UrgencyLevel getUrgency() { return urgency; }
    public void setUrgency(UrgencyLevel urgency) { this.urgency = urgency; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getRequestedByZone() { return requestedByZone; }
    public void setRequestedByZone(String requestedByZone) { this.requestedByZone = requestedByZone; }

    // Builder class
    public static class Builder {
        private String resourceName;
        private Integer quantity;
        private String unit;
        private UrgencyLevel urgency;
        private String requestedBy;
        private String requestedByZone;

        public Builder resourceName(String resourceName) { this.resourceName = resourceName; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder unit(String unit) { this.unit = unit; return this; }
        public Builder urgency(UrgencyLevel urgency) { this.urgency = urgency; return this; }
        public Builder requestedBy(String requestedBy) { this.requestedBy = requestedBy; return this; }
        public Builder requestedByZone(String requestedByZone) { this.requestedByZone = requestedByZone; return this; }

        public ResourceRequestDto build() {
            return new ResourceRequestDto(resourceName, quantity, unit, urgency, requestedBy, requestedByZone);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}