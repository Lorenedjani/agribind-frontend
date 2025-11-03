package cm.agribind.usermanagement.dto.command;

import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import cm.agribind.usermanagement.enums.Region;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterFarmerCommand {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotNull(message = "Region is required")
    private Region region;

    private String department;
    private String district;
    private String village;

    @NotNull(message = "Agricultural type is required")
    private AgriculturalType agriculturalType;

    private Set<CropType> cropTypes;
    private Set<LivestockType> livestockTypes;

    @NotNull(message = "Land area is required")
    private Double landArea;

    private Long cooperativeId;
    private Integer yearsFarming;
    private String educationLevel;
    private Boolean hasBankAccount = false;

    private String preferredLanguage = "fr";
    private String gpsCoordinates;
}