package cm.agribind.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "farmer_qr_codes", indexes = {
        @Index(name = "idx_farmer", columnList = "farmer_id"),
        @Index(name = "idx_hash", columnList = "qr_code_hash"),
        @Index(name = "idx_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerQrCode {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "farmer_id", nullable = false, length = 36)
    private String farmerId;

    @Column(name = "qr_code_hash", unique = true, nullable = false)
    private String qrCodeHash;

    @Lob
    @Column(name = "qr_code_image", columnDefinition = "LONGBLOB")
    private byte[] qrCodeImage;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Builder.Default
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
