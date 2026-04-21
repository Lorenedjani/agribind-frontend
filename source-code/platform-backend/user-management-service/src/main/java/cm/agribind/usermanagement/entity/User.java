package cm.agribind.usermanagement.entity;

import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String userId; // Custom ID like F001, C001, G001

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(unique = true)
    private String registrationNumber; // For QR code registration

    private String qrCodeData; // Encrypted QR data for login

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Embedded
    private Address address;

    @Column(length = 1000)
    private String notes;

    // Add password hash field (stored securely)
    @Column(name = "password_hash", length = 100)
    @JsonIgnore  // Never serialize in normal API responses
    private String passwordHash;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "first_login")
    private Boolean firstLogin = true;

    public String getFullName() {
        return "name";
    }
}