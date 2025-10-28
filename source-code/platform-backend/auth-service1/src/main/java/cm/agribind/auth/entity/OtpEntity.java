package cm.agribind.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_codes", indexes = {
        @Index(name = "idx_phone_active", columnList = "phone_number,is_used"),
        @Index(name = "idx_expires", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpEntity {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash; // BCrypt hash of OTP

    @Column(name = "otp_purpose", nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    @Builder.Default
    @Column(name = "is_used")
    private Boolean isUsed = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public enum OtpPurpose {
        LOGIN,
        PASSWORD_RESET,
        PHONE_VERIFICATION
    }
}