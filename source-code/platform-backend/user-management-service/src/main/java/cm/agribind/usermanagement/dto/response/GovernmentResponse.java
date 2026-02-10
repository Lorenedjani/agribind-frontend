package cm.agribind.usermanagement.dto.response;

import cm.agribind.usermanagement.enums.GovernmentRole;
import jakarta.persistence.Column;
import cm.agribind.usermanagement.enums.Region;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GovernmentResponse {

    private GovernmentRole role;
    private Region assignedRegion;
    private String department;
    private String employeeId;
    private String jurisdiction;

    // Office information
    private String officeLocation;
    private String officePhone;
    private String officialEmail;

    // Permissions
    private Boolean canApproveLoans;
    private Boolean canViewStatistics;

    // Organizational information
    private String rank;
    private String employmentDate;
    private String responsibilities;
    private String supervisor;

    // Field work
    private Boolean isFieldOfficer;
    private String vehicleAssignment;
    private String assignedEquipment;

    // Display information
    private String displayDetails; // e.g., "Regional Coordinator Ministry of Agriculture"
}