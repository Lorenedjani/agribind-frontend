package cm.agribind.usermanagement.dto.query;

import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import lombok.Data;

@Data
public class UserFilterQuery {

    // Common filters
    private UserType type;
    private UserStatus status;
    private Region region;
    private String searchTerm;

    // Farmer specific filters
    private AgriculturalType agriculturalType;
    private CropType cropType;
    private LivestockType livestockType;
    private Long cooperativeId;
    private Double minLandArea;
    private Double maxLandArea;
    private Boolean hasBankAccount;
    private Boolean hasMobileMoney;

    // Cooperative specific filters
    private String cooperativeType;
    private Integer minMembers;
    private Integer maxMembers;

    // Government specific filters
    private String governmentRole;
    private String department;

    // Pagination
    private Integer page = 0;
    private Integer size = 50;
    private String sortBy = "name";
    private String sortDirection = "ASC";
}