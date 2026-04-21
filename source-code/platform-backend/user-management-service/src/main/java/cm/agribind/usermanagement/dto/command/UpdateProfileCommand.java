package cm.agribind.usermanagement.dto.command;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateProfileCommand {

    private String bio;
    private String preferredLanguage;
    private Boolean receiveSmsNotifications;
    private Boolean receiveEmailNotifications;
    private Boolean receivePushNotifications;
    private String skills;
    private String dateOfBirth;
    private String gender;
    private String maritalStatus;
    private Integer dependentsCount;

    // For file upload
    private transient MultipartFile profilePicture;
}