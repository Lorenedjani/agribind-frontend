package cm.agribind.usermanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

// ===== BASE USER ENTITY =====

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_phone", columnList = "phone_number"),
        @Index(name = "idx_active_role", columnList = "is_active,role")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(length = 36)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private String address;

    @Column(name = "preferred_language", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Language preferredLanguage = Language.FR;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "password_hash")
    private String passwordHash;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_first_login")
    private Boolean isFirstLogin = true;

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @Column(name = "deactivated_by", length = 36)
    private String deactivatedBy;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
