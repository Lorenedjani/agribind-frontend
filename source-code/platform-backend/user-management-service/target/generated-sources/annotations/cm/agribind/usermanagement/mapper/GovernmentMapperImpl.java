package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.response.GovernmentResponse;
import cm.agribind.usermanagement.entity.GovernmentDetails;
import cm.agribind.usermanagement.entity.GovernmentOfficial;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-10T03:08:30+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class GovernmentMapperImpl implements GovernmentMapper {

    @Override
    public GovernmentResponse toResponse(GovernmentOfficial government) {
        if ( government == null ) {
            return null;
        }

        GovernmentResponse governmentResponse = new GovernmentResponse();

        governmentResponse.setDisplayDetails( toDisplayDetails( government ) );
        governmentResponse.setOfficeLocation( governmentGovernmentDetailsOfficeLocation( government ) );
        governmentResponse.setOfficePhone( governmentGovernmentDetailsOfficePhone( government ) );
        governmentResponse.setOfficialEmail( governmentGovernmentDetailsOfficialEmail( government ) );
        governmentResponse.setRank( governmentGovernmentDetailsRank( government ) );
        governmentResponse.setEmploymentDate( governmentGovernmentDetailsEmploymentDate( government ) );
        governmentResponse.setResponsibilities( governmentGovernmentDetailsResponsibilities( government ) );
        governmentResponse.setSupervisor( governmentGovernmentDetailsReportsTo( government ) );
        governmentResponse.setIsFieldOfficer( governmentGovernmentDetailsIsFieldOfficer( government ) );
        governmentResponse.setVehicleAssignment( governmentGovernmentDetailsVehicleAssignment( government ) );
        governmentResponse.setAssignedEquipment( governmentGovernmentDetailsAssignedEquipment( government ) );
        governmentResponse.setAssignedRegion( government.getAssignedRegion() );
        governmentResponse.setCanApproveLoans( government.getCanApproveLoans() );
        governmentResponse.setCanViewStatistics( government.getCanViewStatistics() );
        governmentResponse.setDepartment( government.getDepartment() );
        governmentResponse.setEmployeeId( government.getEmployeeId() );
        governmentResponse.setJurisdiction( government.getJurisdiction() );
        governmentResponse.setRole( government.getRole() );

        return governmentResponse;
    }

    private String governmentGovernmentDetailsOfficeLocation(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String officeLocation = governmentDetails.getOfficeLocation();
        if ( officeLocation == null ) {
            return null;
        }
        return officeLocation;
    }

    private String governmentGovernmentDetailsOfficePhone(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String officePhone = governmentDetails.getOfficePhone();
        if ( officePhone == null ) {
            return null;
        }
        return officePhone;
    }

    private String governmentGovernmentDetailsOfficialEmail(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String officialEmail = governmentDetails.getOfficialEmail();
        if ( officialEmail == null ) {
            return null;
        }
        return officialEmail;
    }

    private String governmentGovernmentDetailsRank(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String rank = governmentDetails.getRank();
        if ( rank == null ) {
            return null;
        }
        return rank;
    }

    private String governmentGovernmentDetailsEmploymentDate(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String employmentDate = governmentDetails.getEmploymentDate();
        if ( employmentDate == null ) {
            return null;
        }
        return employmentDate;
    }

    private String governmentGovernmentDetailsResponsibilities(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String responsibilities = governmentDetails.getResponsibilities();
        if ( responsibilities == null ) {
            return null;
        }
        return responsibilities;
    }

    private String governmentGovernmentDetailsReportsTo(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String reportsTo = governmentDetails.getReportsTo();
        if ( reportsTo == null ) {
            return null;
        }
        return reportsTo;
    }

    private Boolean governmentGovernmentDetailsIsFieldOfficer(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        Boolean isFieldOfficer = governmentDetails.getIsFieldOfficer();
        if ( isFieldOfficer == null ) {
            return null;
        }
        return isFieldOfficer;
    }

    private String governmentGovernmentDetailsVehicleAssignment(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String vehicleAssignment = governmentDetails.getVehicleAssignment();
        if ( vehicleAssignment == null ) {
            return null;
        }
        return vehicleAssignment;
    }

    private String governmentGovernmentDetailsAssignedEquipment(GovernmentOfficial governmentOfficial) {
        if ( governmentOfficial == null ) {
            return null;
        }
        GovernmentDetails governmentDetails = governmentOfficial.getGovernmentDetails();
        if ( governmentDetails == null ) {
            return null;
        }
        String assignedEquipment = governmentDetails.getAssignedEquipment();
        if ( assignedEquipment == null ) {
            return null;
        }
        return assignedEquipment;
    }
}
