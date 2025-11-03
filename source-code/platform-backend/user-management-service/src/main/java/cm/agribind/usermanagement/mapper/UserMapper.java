package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.*;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {ProfileMapper.class}, // Remove AddressMapper from uses
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "registrationNumber", ignore = true)
    @Mapping(target = "qrCodeData", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "address", source = ".", qualifiedByName = "toAddress")
    User toEntity(CreateUserCommand command);

    @Mapping(target = "fullAddress", source = "address", qualifiedByName = "toFullAddress")
    @Mapping(target = "profilePictureUrl", source = "profile.profilePicturePath")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "registrationNumber", ignore = true)
    @Mapping(target = "qrCodeData", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "address", source = ".", qualifiedByName = "toAddressFromUpdate")
    void updateEntityFromCommand(UpdateUserCommand command, @MappingTarget User user);

    @Named("toFullAddress")
    default String toFullAddress(Address address) {
        if (address == null) {
            return "";
        }
        return address.getFullAddress();
    }

    @Named("toAddress")
    default Address toAddress(CreateUserCommand command) {
        if (command.getRegion() == null &&
                command.getDepartment() == null &&
                command.getDistrict() == null &&
                command.getVillage() == null) {
            return null;
        }

        Address address = new Address();
        address.setRegion(command.getRegion());
        address.setDepartment(command.getDepartment());
        address.setDistrict(command.getDistrict());
        address.setVillage(command.getVillage());
        address.setGpsCoordinates(command.getGpsCoordinates());
        return address;
    }

    @Named("toAddressFromUpdate")
    default Address toAddressFromUpdate(UpdateUserCommand command, @MappingTarget User user) {
        Address existingAddress = user.getAddress();
        if (existingAddress == null) {
            existingAddress = new Address();
        }

        if (command.getRegion() != null) {
            existingAddress.setRegion(command.getRegion());
        }
        if (command.getDepartment() != null) {
            existingAddress.setDepartment(command.getDepartment());
        }
        if (command.getDistrict() != null) {
            existingAddress.setDistrict(command.getDistrict());
        }
        if (command.getVillage() != null) {
            existingAddress.setVillage(command.getVillage());
        }
        if (command.getGpsCoordinates() != null) {
            existingAddress.setGpsCoordinates(command.getGpsCoordinates());
        }

        return existingAddress;
    }

    @AfterMapping
    default void afterUserMapping(@MappingTarget UserResponse response, User user) {
        // Set quick action flags
        response.setCanGenerateQR(user.getStatus() == UserStatus.ACTIVE);
        response.setCanExport(true);
        response.setCanEdit(true);

        // Set type-specific responses
        if (user instanceof Farmer farmer) {
            response.setFarmerDetails(FarmerMapper.INSTANCE.toResponse(farmer));
        } else if (user instanceof Cooperative cooperative) {
            response.setCooperativeDetails(CooperativeMapper.INSTANCE.toResponse(cooperative));
        } else if (user instanceof GovernmentOfficial government) {
            response.setGovernmentDetails(GovernmentMapper.INSTANCE.toResponse(government));
        }
    }
}