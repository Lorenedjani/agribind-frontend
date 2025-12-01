package com.agribind.communication.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ==================== Communication Statistics Entity ====================
@Entity
@Table(name = "communication_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunicationStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime statDate;

    private Integer totalMessagesSent;
    private Integer messagesSentThisWeek;

    private Integer audioMessagesTotal;
    private Integer audioLanguagesSupported;

    private Integer activeAlerts;
    private Integer criticalAlerts;

    private Double deliveryRate;
    private Double deliveryRateChange;

    private Integer activeMembers;
    private Double activeLoansAmount;
    private Integer lowStockAlerts;

    @PrePersist
    protected void onCreate() {
        statDate = LocalDateTime.now();
    }
}