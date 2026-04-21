package cm.agribind.usermanagement.dto.response;

import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FarmerResponse {

    private AgriculturalType agriculturalType;
    private Set<CropType> cropTypes;
    private Set<LivestockType> livestockTypes;

    // Farm details
    private Double totalLandArea;
    private Double cultivatedArea;
    private String soilType;
    private String irrigationType;
    private Boolean ownsLand;
    private String landOwnershipType;

    // Cooperative information
    private Long cooperativeId;
    private String cooperativeName;

    // Additional farmer info
    private Integer yearsFarming;
    private String educationLevel;
    private Boolean hasBankAccount;
    private Boolean hasMobileMoney;

    // Display details for frontend
    private String displayDetails; // e.g., "Cocoa 5.3 ha"
    private String cropTypesDisplay;
    private String livestockTypesDisplay;
}