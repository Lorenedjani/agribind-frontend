package com.agribind.communication.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ==================== Message Entity ====================
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MessageType type; // SMS, AUDIO

    @Enumerated(EnumType.STRING)
    private MessagePriority priority;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String audioFileUrl;

    @Enumerated(EnumType.STRING)
    private TargetAudience targetAudience;

    private String specificZone; // DOUALA, YAOUNDE, OTHER_REGIONS

    @ElementCollection
    @CollectionTable(name = "message_channels", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "channel")
    private List<String> channels = new ArrayList<>(); // SMS, PUSH, AUDIO

    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    private Integer totalRecipients;
    private Integer deliveredCount;
    private Integer failedCount;

    private Double estimatedCost;
    private Double actualCost;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = MessageStatus.DRAFT;
        deliveredCount = 0;
        failedCount = 0;
    }
}
