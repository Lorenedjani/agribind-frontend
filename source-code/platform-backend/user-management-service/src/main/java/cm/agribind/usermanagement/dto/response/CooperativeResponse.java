package cm.agribind.usermanagement.dto.response;

import cm.agribind.usermanagement.enums.CooperativeType;
import cm.agribind.usermanagement.enums.Region;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CooperativeResponse {

    private CooperativeType cooperativeType;
    private Region operatingRegion;

    // Cooperative details
    private String legalRegistrationNumber;
    private Integer establishmentYear;
    private String contactPerson;
    private String contactPersonPhone;

    // Statistics
    private Integer totalMembers;
    private Integer femaleMembers;
    private Integer maleMembers;
    private Integer activeMemberCount;
    private Double totalLandArea;
    private Double annualProduction;
    private Double annualRevenue;

    private String primaryProducts;
    private String certification;

    // Facilities
    private Boolean hasStorageFacilities;
    private Boolean hasProcessingEquipment;
    private Boolean hasTransportVehicles;

    // Financial information
    private String bankName;
    private String bankAccountNumber;
    private String mobileMoneyNumber;

    // Display information
    private String displayDetails; // e.g., "156 members Est. 2018"
}