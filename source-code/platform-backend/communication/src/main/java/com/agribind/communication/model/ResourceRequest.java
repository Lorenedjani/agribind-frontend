package com.agribind.communication.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// ==================== Resource Request Entity ====================
@Entity
@Table(name = "resource_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId; // REQ-001

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
}