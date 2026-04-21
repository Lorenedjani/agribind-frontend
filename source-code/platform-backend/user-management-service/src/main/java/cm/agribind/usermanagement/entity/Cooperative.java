package cm.agribind.usermanagement.entity;

import cm.agribind.usermanagement.enums.CooperativeType;
import cm.agribind.usermanagement.enums.Region;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cooperatives")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class Cooperative extends User {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CooperativeType cooperativeType;

    @Embedded
    private CooperativeDetails cooperativeDetails;

    @OneToMany(mappedBy = "cooperative", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Farmer> members = new ArrayList<>();

    @OneToMany(mappedBy = "cooperative", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CooperativeManager> managers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region operatingRegion;

    private String legalRegistrationNumber;

    private Integer establishmentYear;

    private String contactPerson;

    private String contactPersonPhone;

    private Double totalLandArea; // Total area managed by cooperative

    private Integer activeMemberCount;
}