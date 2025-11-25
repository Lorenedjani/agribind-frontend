package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.*;
import cm.agribind.usermanagement.enums.UserStatus;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {ProfileMapper.class},
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

    // ✅ CRITICAL FIX: Explicit auth field mappings with proper handling
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "accountLocked", source = "accountLocked")
    @Mapping(target = "accountEnabled", expression = "java(user.getStatus() == cm.agribind.usermanagement.enums.UserStatus.ACTIVE)")
    @Mapping(target = "failedLoginAttempts", source = "failedLoginAttempts")
    @Mapping(target = "firstLogin", source = "firstLogin")
    //@Mapping(target = "lastLoginAt", source = "lastLoginAt")
    //@Mapping(target = "lastPasswordChange", source = "lastPasswordChange")
    @Mapping(target = "fullAddress", source = "address", qualifiedByName = "toFullAddress")
    @Mapping(target = "profilePictureUrl", source = "profile.profilePicturePath")
    @Mapping(target = "preferredLanguage", source = "profile.preferredLanguage")
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
    default Address toAddressFromUpdate(UpdateUserCommand command) {
        if (command.getRegion() == null &&
                command.getDepartment() == null &&
                command.getDepartment() == null &&
                command.getVillage() == null &&
                command.getGpsCoordinates() == null) {
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

    @AfterMapping
    default void afterUpdateMapping(@MappingTarget User user, UpdateUserCommand command) {
        if (user.getAddress() != null && command != null) {
            Address existingAddress = user.getAddress();

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
        }
    }

    @AfterMapping
    default void afterUserMapping(@MappingTarget UserResponse response, User user) {
        // ✅ CRITICAL: Ensure auth fields have proper defaults
        if (response.getAccountLocked() == null) {
            response.setAccountLocked(false);
        }
        if (response.getAccountEnabled() == null) {
            response.setAccountEnabled(user.getStatus() == UserStatus.ACTIVE);
        }
        if (response.getFailedLoginAttempts() == null) {
            response.setFailedLoginAttempts(0);
        }
        if (response.getFirstLogin() == null) {
            response.setFirstLogin(true);
        }

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