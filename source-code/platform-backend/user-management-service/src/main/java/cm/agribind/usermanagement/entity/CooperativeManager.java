package cm.agribind.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "cooperative_managers", indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_cooperative", columnList = "cooperative_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CooperativeManager {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", unique = true, nullable = false, length = 36)
    private String userId;

    @Column(name = "cooperative_id", nullable = false, length = 36)
    private String cooperativeId;

    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
