package cm.agribind.usermanagement.dto.command;

import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserCommand {

    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;

    private UserStatus status;

    private Region region;
    private String department;
    private String district;
    private String village;
    private String gpsCoordinates;

    // Farmer updates
    private String agriculturalType;
    private String[] cropTypes;
    private String[] livestockTypes;
    private Double landArea;
    private Long cooperativeId;

    // Cooperative updates
    private Integer activeMemberCount;
    private Double annualProduction;

    private String notes;
}