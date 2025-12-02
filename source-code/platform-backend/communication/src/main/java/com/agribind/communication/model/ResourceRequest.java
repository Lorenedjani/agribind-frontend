package com.agribind.communication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_requests")
public class ResourceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId;

    private String resourceName;
    private Integer quantity;
    private String unit;

    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgency;

    private String requestedBy;
    private String requestedByZone;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private Integer suppliersMatched;

    private LocalDateTime requestDate;
    private LocalDateTime fulfilledDate;

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now();
        status = RequestStatus.PENDING;
        suppliersMatched = 0;
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

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public UrgencyLevel getUrgency() { return urgency; }
    public void setUrgency(UrgencyLevel urgency) { this.urgency = urgency; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getRequestedByZone() { return requestedByZone; }
    public void setRequestedByZone(String requestedByZone) { this.requestedByZone = requestedByZone; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public Integer getSuppliersMatched() { return suppliersMatched; }
    public void setSuppliersMatched(Integer suppliersMatched) { this.suppliersMatched = suppliersMatched; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getFulfilledDate() { return fulfilledDate; }
    public void setFulfilledDate(LocalDateTime fulfilledDate) { this.fulfilledDate = fulfilledDate; }
}