package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.response.CooperativeResponse;
import cm.agribind.usermanagement.entity.Cooperative;
import cm.agribind.usermanagement.entity.CooperativeDetails;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        uses = {AddressMapper.class, ProfileMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CooperativeMapper {

    CooperativeMapper INSTANCE = Mappers.getMapper(CooperativeMapper.class);

    @Mapping(target = "displayDetails", source = "cooperative", qualifiedByName = "toDisplayDetails")
    @Mapping(target = "totalMembers", source = "cooperativeDetails.totalMembers")
    @Mapping(target = "femaleMembers", source = "cooperativeDetails.femaleMembers")
    @Mapping(target = "maleMembers", source = "cooperativeDetails.maleMembers")
    @Mapping(target = "annualProduction", source = "cooperativeDetails.annualProduction")
    @Mapping(target = "annualRevenue", source = "cooperativeDetails.annualRevenue")
    @Mapping(target = "primaryProducts", source = "cooperativeDetails.primaryProducts")
    @Mapping(target = "certification", source = "cooperativeDetails.certification")
    @Mapping(target = "hasStorageFacilities", source = "cooperativeDetails.hasStorageFacilities")
    @Mapping(target = "hasProcessingEquipment", source = "cooperativeDetails.hasProcessingEquipment")
    @Mapping(target = "hasTransportVehicles", source = "cooperativeDetails.hasTransportVehicles")
    @Mapping(target = "bankName", source = "cooperativeDetails.bankName")
    @Mapping(target = "bankAccountNumber", source = "cooperativeDetails.bankAccountNumber")
    @Mapping(target = "mobileMoneyNumber", source = "cooperativeDetails.mobileMoneyNumber")
    CooperativeResponse toResponse(Cooperative cooperative);

    @Named("toDisplayDetails")
    default String toDisplayDetails(Cooperative cooperative) {
        if (cooperative == null) {
            return "";
        }

        StringBuilder details = new StringBuilder();

        // Add member count
        if (cooperative.getActiveMemberCount() != null) {
            details.append(cooperative.getActiveMemberCount()).append(" members");
        } else if (cooperative.getCooperativeDetails() != null &&
                cooperative.getCooperativeDetails().getTotalMembers() != null) {
            details.append(cooperative.getCooperativeDetails().getTotalMembers()).append(" members");
        }

        // Add establishment year
        if (cooperative.getEstablishmentYear() != null) {
            details.append(" Est. ").append(cooperative.getEstablishmentYear());
        }

        return details.toString();
    }

    @AfterMapping
    default void afterCooperativeMapping(@MappingTarget CooperativeResponse response, Cooperative cooperative) {
        // Ensure active member count is set
        if (response.getActiveMemberCount() == null && cooperative.getMembers() != null) {
            response.setActiveMemberCount(cooperative.getMembers().size());
        }
    }
}