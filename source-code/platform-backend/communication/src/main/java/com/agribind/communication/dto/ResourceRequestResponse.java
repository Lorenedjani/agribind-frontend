package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class ResourceRequestResponse {
    private Long id;
    private String requestId;
    private String resourceName;
    private Integer quantity;
    private UrgencyLevel urgency;
    private RequestStatus status;
    private Integer suppliersMatched;
    private LocalDateTime requestDate;

    // Default constructor
    public ResourceRequestResponse() {}

    // Builder pattern constructor
    private ResourceRequestResponse(Builder builder) {
        this.id = builder.id;
        this.requestId = builder.requestId;
        this.resourceName = builder.resourceName;
        this.quantity = builder.quantity;
        this.urgency = builder.urgency;
        this.status = builder.status;
        this.suppliersMatched = builder.suppliersMatched;
        this.requestDate = builder.requestDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public UrgencyLevel getUrgency() { return urgency; }
    public void setUrgency(UrgencyLevel urgency) { this.urgency = urgency; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public Integer getSuppliersMatched() { return suppliersMatched; }
    public void setSuppliersMatched(Integer suppliersMatched) { this.suppliersMatched = suppliersMatched; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    // Builder class
    public static class Builder {
        private Long id;
        private String requestId;
        private String resourceName;
        private Integer quantity;
        private UrgencyLevel urgency;
        private RequestStatus status;
        private Integer suppliersMatched;
        private LocalDateTime requestDate;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder resourceName(String resourceName) { this.resourceName = resourceName; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder urgency(UrgencyLevel urgency) { this.urgency = urgency; return this; }
        public Builder status(RequestStatus status) { this.status = status; return this; }
        public Builder suppliersMatched(Integer suppliersMatched) { this.suppliersMatched = suppliersMatched; return this; }
        public Builder requestDate(LocalDateTime requestDate) { this.requestDate = requestDate; return this; }

        public ResourceRequestResponse build() {
            return new ResourceRequestResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}