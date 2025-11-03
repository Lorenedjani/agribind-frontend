package cm.agribind.usermanagement.dto.command;

import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserCommand {

    @NotNull(message = "User type is required")
    private UserType type;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;

    private Region region;
    private String department;
    private String district;
    private String village;
    private String gpsCoordinates;

    private String preferredLanguage = "fr";

    // Farmer specific fields
    private String agriculturalType;
    private String[] cropTypes;
    private String[] livestockTypes;
    private Double landArea;
    private Long cooperativeId;

    // Cooperative specific fields
    private String cooperativeType;
    private String legalRegistrationNumber;
    private Integer establishmentYear;
    private String contactPerson;

    // Government specific fields
    private String governmentRole;
    private String departmentName;
    private String employeeId;

    private String notes;
}