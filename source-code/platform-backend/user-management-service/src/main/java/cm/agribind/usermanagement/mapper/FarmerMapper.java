package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.RegisterFarmerCommand;
import cm.agribind.usermanagement.dto.response.FarmerResponse;
import cm.agribind.usermanagement.entity.Farmer;
import cm.agribind.usermanagement.entity.FarmDetails;
import cm.agribind.usermanagement.enums.AgriculturalType;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {ProfileMapper.class}, // Remove AddressMapper
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface FarmerMapper {

    FarmerMapper INSTANCE = Mappers.getMapper(FarmerMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "type", constant = "FARMER")
    @Mapping(target = "registrationNumber", ignore = true)
    @Mapping(target = "qrCodeData", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "cooperative", ignore = true)
    @Mapping(target = "farmDetails", source = "command")
    @Mapping(target = "address", source = ".", qualifiedByName = "toAddress")
    Farmer toEntity(RegisterFarmerCommand command);

    @Mapping(target = "displayDetails", source = "farmer", qualifiedByName = "toDisplayDetails")
    @Mapping(target = "cropTypesDisplay", source = "cropTypes", qualifiedByName = "cropsToString")
    @Mapping(target = "livestockTypesDisplay", source = "livestockTypes", qualifiedByName = "livestockToString")
    @Mapping(target = "totalLandArea", source = "farmDetails.totalLandArea")
    @Mapping(target = "cultivatedArea", source = "farmDetails.cultivatedArea")
    @Mapping(target = "soilType", source = "farmDetails.soilType")
    @Mapping(target = "irrigationType", source = "farmDetails.irrigationType")
    @Mapping(target = "ownsLand", source = "farmDetails.ownsLand")
    @Mapping(target = "landOwnershipType", source = "farmDetails.landOwnershipType")
    @Mapping(target = "cooperativeName", source = "cooperative.name")
    FarmerResponse toResponse(Farmer farmer);

    @Named("toAddress")
    default cm.agribind.usermanagement.entity.Address toAddress(RegisterFarmerCommand command) {
        if (command.getRegion() == null &&
                command.getDepartment() == null &&
                command.getDistrict() == null &&
                command.getVillage() == null) {
            return null;
        }

        cm.agribind.usermanagement.entity.Address address = new cm.agribind.usermanagement.entity.Address();
        address.setRegion(command.getRegion());
        address.setDepartment(command.getDepartment());
        address.setDistrict(command.getDistrict());
        address.setVillage(command.getVillage());
        address.setGpsCoordinates(command.getGpsCoordinates());
        return address;
    }

    @Named("toDisplayDetails")
    default String toDisplayDetails(Farmer farmer) {
        if (farmer == null) {
            return "";
        }

        StringBuilder details = new StringBuilder();

        // Add primary crop or livestock
        if (farmer.getAgriculturalType() == AgriculturalType.CROP &&
                !farmer.getCropTypes().isEmpty()) {
            String primaryCrop = farmer.getCropTypes().iterator().next().name();
            details.append(primaryCrop);
        } else if (farmer.getAgriculturalType() == AgriculturalType.LIVESTOCK &&
                !farmer.getLivestockTypes().isEmpty()) {
            String primaryLivestock = farmer.getLivestockTypes().iterator().next().name();
            details.append(primaryLivestock);
        } else if (farmer.getAgriculturalType() == AgriculturalType.MIXED) {
            details.append("Mixed Farming");
        }

        // Add land area
        if (farmer.getFarmDetails() != null && farmer.getFarmDetails().getTotalLandArea() != null) {
            details.append(" ").append(farmer.getFarmDetails().getTotalLandArea()).append(" ha");
        }

        return details.toString();
    }

    @Named("cropsToString")
    default String cropsToString(Set<cm.agribind.usermanagement.enums.CropType> cropTypes) {
        if (cropTypes == null || cropTypes.isEmpty()) {
            return "";
        }
        return cropTypes.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    @Named("livestockToString")
    default String livestockToString(Set<cm.agribind.usermanagement.enums.LivestockType> livestockTypes) {
        if (livestockTypes == null || livestockTypes.isEmpty()) {
            return "";
        }
        return livestockTypes.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "farmDetails.totalLandArea", source = "landArea")
    void updateEntityFromCommand(RegisterFarmerCommand command, @MappingTarget Farmer farmer);

    @AfterMapping
    default void afterFarmerMapping(RegisterFarmerCommand command, @MappingTarget Farmer farmer) {
        // Initialize farm details if null
        if (farmer.getFarmDetails() == null) {
            farmer.setFarmDetails(new FarmDetails());
        }

        // Set land area
        if (command.getLandArea() != null) {
            farmer.getFarmDetails().setTotalLandArea(command.getLandArea());
            farmer.getFarmDetails().setCultivatedArea(command.getLandArea());
        }
    }
}