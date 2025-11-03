package cm.agribind.usermanagement.dto.event;

import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.Region;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class FarmerRegisteredEvent {

    private String eventId;
    private String farmerId;
    private String name;
    private String phoneNumber;
    private Region region;
    private AgriculturalType agriculturalType;
    private Set<String> cropTypes;
    private Set<String> livestockTypes;
    private Double landArea;
    private Long cooperativeId;
    private String preferredLanguage;

    private String source = "USER_MANAGEMENT_SERVICE";
    private LocalDateTime eventTime = LocalDateTime.now();
    private String eventType = "FARMER_REGISTERED";

    public FarmerRegisteredEvent(String farmerId, String name, String phoneNumber, Region region) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.farmerId = farmerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.region = region;
        this.eventTime = LocalDateTime.now();
    }
}