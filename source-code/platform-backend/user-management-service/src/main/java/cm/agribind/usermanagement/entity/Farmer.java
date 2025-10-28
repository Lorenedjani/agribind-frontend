package cm.agribind.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "farmers", indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_cooperative", columnList = "cooperative_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_reg_number", columnList = "registration_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", unique = true, nullable = false, length = 36)
    private String userId;

    @Column(name = "cooperative_id", nullable = false, length = 36)
    private String cooperativeId;

    @Column(name = "registration_number", unique = true, nullable = false, length = 20)
    private String registrationNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Builder.Default
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "activation_method", length = 30)
    @Enumerated(EnumType.STRING)
    private ActivationMethod activationMethod;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "activated_by", length = 36)
    private String activatedBy;
}
