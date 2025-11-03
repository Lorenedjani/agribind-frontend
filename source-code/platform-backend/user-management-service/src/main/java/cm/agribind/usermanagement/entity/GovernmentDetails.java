package cm.agribind.usermanagement.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class GovernmentDetails {

    private String officeLocation;

    private String officePhone;

    private String officialEmail;

    private String rank;

    private String employmentDate;

    private String responsibilities; // Comma-separated

    private String projectsManaged;

    private String reportsTo; // Supervisor name

    private String extensionNumber;

    private Boolean isFieldOfficer = true;

    private String vehicleAssignment;

    private String assignedEquipment;
}