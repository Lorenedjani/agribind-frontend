package com.agribind.production_monitoring.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "maturity_updates")
public class MaturityUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_record_id", nullable = false)
    private Long productionRecordId;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status", nullable = false)
    private String newStatus;

    // VERIFIED: This is String type as per our updates
    @Column(name = "updated_by_farmer_id", nullable = false, length = 20)
    private String updatedByFarmerId;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        updateDate = LocalDateTime.now();
    }
}