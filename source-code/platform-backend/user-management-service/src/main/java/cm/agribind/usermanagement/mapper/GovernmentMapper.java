package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.response.GovernmentResponse;
import cm.agribind.usermanagement.entity.GovernmentOfficial;
import cm.agribind.usermanagement.entity.GovernmentDetails;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        uses = {AddressMapper.class, ProfileMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface GovernmentMapper {

    GovernmentMapper INSTANCE = Mappers.getMapper(GovernmentMapper.class);

    @Mapping(target = "displayDetails", source = "government", qualifiedByName = "toDisplayDetails")
    @Mapping(target = "officeLocation", source = "governmentDetails.officeLocation")
    @Mapping(target = "officePhone", source = "governmentDetails.officePhone")
    @Mapping(target = "officialEmail", source = "governmentDetails.officialEmail")
    @Mapping(target = "rank", source = "governmentDetails.officialRank")
    @Mapping(target = "employmentDate", source = "governmentDetails.employmentDate")
    @Mapping(target = "responsibilities", source = "governmentDetails.responsibilities")
    @Mapping(target = "supervisor", source = "governmentDetails.reportsTo")
    @Mapping(target = "isFieldOfficer", source = "governmentDetails.isFieldOfficer")
    @Mapping(target = "vehicleAssignment", source = "governmentDetails.vehicleAssignment")
    @Mapping(target = "assignedEquipment", source = "governmentDetails.assignedEquipment")
    GovernmentResponse toResponse(GovernmentOfficial government);

    @Named("toDisplayDetails")
    default String toDisplayDetails(GovernmentOfficial government) {
        if (government == null) {
            return "";
        }

        StringBuilder details = new StringBuilder();

        // Add role
        if (government.getRole() != null) {
            String roleName = government.getRole().name().toLowerCase().replace("_", " ");
            details.append(roleName.substring(0, 1).toUpperCase())
                    .append(roleName.substring(1));
        }

        // Add department
        if (government.getDepartment() != null) {
            details.append(" ").append(government.getDepartment());
        }

        return details.toString();
    }
}