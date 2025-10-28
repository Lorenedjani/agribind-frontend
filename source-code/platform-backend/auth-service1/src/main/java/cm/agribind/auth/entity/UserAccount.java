package cm.agribind.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String phone;

    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default private Boolean isActive = true;
    @Builder.Default private Boolean isFirstLogin = true;
    @Builder.Default private Instant createdAt = Instant.now();
    @Builder.Default private Instant updatedAt = Instant.now();
}
