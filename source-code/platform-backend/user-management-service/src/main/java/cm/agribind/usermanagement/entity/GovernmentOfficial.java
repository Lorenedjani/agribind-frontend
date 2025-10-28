package cm.agribind.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "government_officials", indexes = {
        @Index(name = "idx_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernmentOfficial {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", unique = true, nullable = false, length = 36)
    private String userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "office_address")
    private String officeAddress;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
