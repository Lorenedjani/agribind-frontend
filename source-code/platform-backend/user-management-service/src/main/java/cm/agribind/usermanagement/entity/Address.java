package cm.agribind.usermanagement.entity;

import cm.agribind.usermanagement.enums.Region;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Address {

    @Enumerated(EnumType.STRING)
    private Region region;

    private String department;

    private String district;

    private String village;

    private String street;

    private String gpsCoordinates; // Latitude,Longitude

    private String landmark;

    private Boolean isRural = true;

    public String getFullAddress() {
        return String.format("%s, %s, %s, %s",
                village != null ? village : "",
                district != null ? district : "",
                department != null ? department : "",
                region != null ? region.name() : "");
    }
}