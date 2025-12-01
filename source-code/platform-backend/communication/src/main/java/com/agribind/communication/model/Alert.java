package com.agribind.communication.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// ==================== Alert Entity ====================
@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String alertId; // ALT-001, ALT-002

    @Enumerated(EnumType.STRING)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    private AlertPriority priority;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "alert_channels", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "channel")
    private List<String> channels = new ArrayList<>();

    private Integer recipientCount;

    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    private Double deliveryRate;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        deliveryRate = 0.0;
    }
}
