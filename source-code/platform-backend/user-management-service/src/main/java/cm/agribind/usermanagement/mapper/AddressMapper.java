package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Named("toFullAddress")
    default String toFullAddress(Address address) {
        if (address == null) {
            return "";
        }
        return address.getFullAddress();
    }

    // Add more mapping methods if needed
    default Address mapAddress(cm.agribind.usermanagement.enums.Region region,
                               String department, String district, String village) {
        if (region == null && department == null && district == null && village == null) {
            return null;
        }

        Address address = new Address();
        address.setRegion(region);
        address.setDepartment(department);
        address.setDistrict(district);
        address.setVillage(village);
        return address;
    }
}