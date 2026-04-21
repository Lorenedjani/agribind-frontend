package cm.agribind.usermanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Getter
@Setter
public class Profile extends BaseEntity {

    @OneToOne(mappedBy = "profile")
    private User user;

    @Column(length = 500)
    private String bio;

    private String profilePicturePath;

    private String preferredLanguage = "fr"; // Default French

    private Boolean receiveSmsNotifications = true;

    private Boolean receiveEmailNotifications = false;

    private Boolean receivePushNotifications = true;

    @Column(length = 1000)
    private String skills; // Comma-separated skills

    private String dateOfBirth;

    private String gender;

    private String maritalStatus;

    private Integer dependentsCount;
}