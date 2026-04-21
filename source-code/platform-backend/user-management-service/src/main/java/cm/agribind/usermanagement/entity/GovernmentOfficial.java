package cm.agribind.usermanagement.entity;

import cm.agribind.usermanagement.enums.GovernmentRole;
import cm.agribind.usermanagement.enums.Region;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "government_officials")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class GovernmentOfficial extends User {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GovernmentRole role;

    @Embedded
    private GovernmentDetails governmentDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region assignedRegion;

    private String department;

    private String employeeId;

    private String jurisdiction; // Specific area of responsibility

    private Boolean canApproveLoans = false;

    private Boolean canViewStatistics = true;

    private String supervisor;
}