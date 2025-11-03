package cm.agribind.usermanagement.entity;

import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import cm.agribind.usermanagement.enums.Region;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "farmers")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class Farmer extends User {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgriculturalType agriculturalType;

    @ElementCollection
    @CollectionTable(name = "farmer_crops", joinColumns = @JoinColumn(name = "farmer_id"))
    @Column(name = "crop_type")
    @Enumerated(EnumType.STRING)
    private Set<CropType> cropTypes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "farmer_livestock", joinColumns = @JoinColumn(name = "farmer_id"))
    @Column(name = "livestock_type")
    @Enumerated(EnumType.STRING)
    private Set<LivestockType> livestockTypes = new HashSet<>();

    @Embedded
    private FarmDetails farmDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperative_id")
    private Cooperative cooperative;

    private Integer yearsFarming;

    private String educationLevel;

    private Boolean hasBankAccount = false;

    private Boolean hasMobileMoney = true;
}