package cm.agribind.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "cooperatives", indexes = {
        @Index(name = "idx_region", columnList = "region"),
        @Index(name = "idx_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cooperative {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "registration_number", unique = true, nullable = false, length = 50)
    private String registrationNumber;

    @Column(name = "headquarters_address", nullable = false)
    private String headquartersAddress;

    @Column(name = "operating_zones", columnDefinition = "JSON", nullable = false)
    private String operatingZones;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

}
