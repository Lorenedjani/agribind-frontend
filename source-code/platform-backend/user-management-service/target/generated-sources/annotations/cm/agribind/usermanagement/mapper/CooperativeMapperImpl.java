package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.response.CooperativeResponse;
import cm.agribind.usermanagement.entity.Cooperative;
import cm.agribind.usermanagement.entity.CooperativeDetails;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-30T19:51:26+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class CooperativeMapperImpl implements CooperativeMapper {

    @Override
    public CooperativeResponse toResponse(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }

        CooperativeResponse cooperativeResponse = new CooperativeResponse();

        cooperativeResponse.setDisplayDetails( toDisplayDetails( cooperative ) );
        cooperativeResponse.setTotalMembers( cooperativeCooperativeDetailsTotalMembers( cooperative ) );
        cooperativeResponse.setFemaleMembers( cooperativeCooperativeDetailsFemaleMembers( cooperative ) );
        cooperativeResponse.setMaleMembers( cooperativeCooperativeDetailsMaleMembers( cooperative ) );
        cooperativeResponse.setAnnualProduction( cooperativeCooperativeDetailsAnnualProduction( cooperative ) );
        cooperativeResponse.setAnnualRevenue( cooperativeCooperativeDetailsAnnualRevenue( cooperative ) );
        cooperativeResponse.setPrimaryProducts( cooperativeCooperativeDetailsPrimaryProducts( cooperative ) );
        cooperativeResponse.setCertification( cooperativeCooperativeDetailsCertification( cooperative ) );
        cooperativeResponse.setHasStorageFacilities( cooperativeCooperativeDetailsHasStorageFacilities( cooperative ) );
        cooperativeResponse.setHasProcessingEquipment( cooperativeCooperativeDetailsHasProcessingEquipment( cooperative ) );
        cooperativeResponse.setHasTransportVehicles( cooperativeCooperativeDetailsHasTransportVehicles( cooperative ) );
        cooperativeResponse.setBankName( cooperativeCooperativeDetailsBankName( cooperative ) );
        cooperativeResponse.setBankAccountNumber( cooperativeCooperativeDetailsBankAccountNumber( cooperative ) );
        cooperativeResponse.setMobileMoneyNumber( cooperativeCooperativeDetailsMobileMoneyNumber( cooperative ) );
        cooperativeResponse.setActiveMemberCount( cooperative.getActiveMemberCount() );
        cooperativeResponse.setContactPerson( cooperative.getContactPerson() );
        cooperativeResponse.setContactPersonPhone( cooperative.getContactPersonPhone() );
        cooperativeResponse.setCooperativeType( cooperative.getCooperativeType() );
        cooperativeResponse.setEstablishmentYear( cooperative.getEstablishmentYear() );
        cooperativeResponse.setLegalRegistrationNumber( cooperative.getLegalRegistrationNumber() );
        cooperativeResponse.setOperatingRegion( cooperative.getOperatingRegion() );
        cooperativeResponse.setTotalLandArea( cooperative.getTotalLandArea() );

        afterCooperativeMapping( cooperativeResponse, cooperative );

        return cooperativeResponse;
    }

    private Integer cooperativeCooperativeDetailsTotalMembers(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        Integer totalMembers = cooperativeDetails.getTotalMembers();
        if ( totalMembers == null ) {
            return null;
        }
        return totalMembers;
    }

    private Integer cooperativeCooperativeDetailsFemaleMembers(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        Integer femaleMembers = cooperativeDetails.getFemaleMembers();
        if ( femaleMembers == null ) {
            return null;
        }
        return femaleMembers;
    }

    private Integer cooperativeCooperativeDetailsMaleMembers(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        Integer maleMembers = cooperativeDetails.getMaleMembers();
        if ( maleMembers == null ) {
            return null;
        }
        return maleMembers;
    }

    private Double cooperativeCooperativeDetailsAnnualProduction(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        Double annualProduction = cooperativeDetails.getAnnualProduction();
        if ( annualProduction == null ) {
            return null;
        }
        return annualProduction;
    }

    private Double cooperativeCooperativeDetailsAnnualRevenue(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        Double annualRevenue = cooperativeDetails.getAnnualRevenue();
        if ( annualRevenue == null ) {
            return null;
        }
        return annualRevenue;
    }

    private String cooperativeCooperativeDetailsPrimaryProducts(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        String primaryProducts = cooperativeDetails.getPrimaryProducts();
        if ( primaryProducts == null ) {
            return null;
        }
        return primaryProducts;
    }

    private String cooperativeCooperativeDetailsCertification(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        String certification = cooperativeDetails.getCertification();
        if ( certification == null ) {
            return null;
        }
        return certification;
    }

    private Boolean cooperativeCooperativeDetailsHasStorageFacilities(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        Boolean hasStorageFacilities = cooperativeDetails.getHasStorageFacilities();
        if ( hasStorageFacilities == null ) {
            return null;
        }
        return hasStorageFacilities;
    }

    private Boolean cooperativeCooperativeDetailsHasProcessingEquipment(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        Boolean hasProcessingEquipment = cooperativeDetails.getHasProcessingEquipment();
        if ( hasProcessingEquipment == null ) {
            return null;
        }
        return hasProcessingEquipment;
    }

    private Boolean cooperativeCooperativeDetailsHasTransportVehicles(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        Boolean hasTransportVehicles = cooperativeDetails.getHasTransportVehicles();
        if ( hasTransportVehicles == null ) {
            return null;
        }
        return hasTransportVehicles;
    }

    private String cooperativeCooperativeDetailsBankName(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        String bankName = cooperativeDetails.getBankName();
        if ( bankName == null ) {
            return null;
        }
        return bankName;
    }

    private String cooperativeCooperativeDetailsBankAccountNumber(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        String bankAccountNumber = cooperativeDetails.getBankAccountNumber();
        if ( bankAccountNumber == null ) {
            return null;
        }
        return bankAccountNumber;
    }

    private String cooperativeCooperativeDetailsMobileMoneyNumber(Cooperative cooperative) {
        if ( cooperative == null ) {
            return null;
        }
        CooperativeDetails cooperativeDetails = cooperative.getCooperativeDetails();
        if ( cooperativeDetails == null ) {
            return null;
        }
        String mobileMoneyNumber = cooperativeDetails.getMobileMoneyNumber();
        if ( mobileMoneyNumber == null ) {
            return null;
        }
        return mobileMoneyNumber;
    }
}
