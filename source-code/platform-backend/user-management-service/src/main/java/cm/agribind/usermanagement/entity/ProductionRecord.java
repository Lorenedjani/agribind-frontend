package cm.agribind.usermanagement.entity;

import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.ProductionStatus;
import cm.agribind.usermanagement.enums.QualityGrade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "production_records")
@Getter
@Setter
public class ProductionRecord extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String productionId; // e.g., PROD001

    @Column(nullable = false)
    private LocalDate deliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropType cropType;

    @Column(nullable = false)
    private Double quantity; // in metric tons (MT)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualityGrade qualityGrade;

    @Column(nullable = false)
    private String warehouse;

    @Column(nullable = false)
    private Double valueXaf; // Value in XAF

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionStatus status = ProductionStatus.PENDING;

    @Column(length = 1000)
    private String notes;

    // Additional fields for tracking
    @Column
    private String verifiedBy;

    @Column
    private LocalDate verificationDate;

    @Column
    private String rejectionReason;

    // Price per MT at time of delivery
    @Column
    private Double pricePerMt;
}