package cm.agribind.usermanagement.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class CooperativeDetails {

    private Integer totalMembers;

    private Integer femaleMembers;

    private Integer maleMembers;

    private Double annualProduction; // in tons

    private Double annualRevenue; // in XAF

    private String primaryProducts; // Comma-separated

    private String certification; // ORGANIC, FAIRTRADE, etc.

    private Boolean hasStorageFacilities = false;

    private Boolean hasProcessingEquipment = false;

    private Boolean hasTransportVehicles = false;

    private String bankName;

    private String bankAccountNumber;

    private String mobileMoneyNumber;
}