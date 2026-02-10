package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.Address;
import cm.agribind.usermanagement.entity.Profile;
import cm.agribind.usermanagement.entity.User;
import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-10T03:16:56+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(CreateUserCommand command) {
        if ( command == null ) {
            return null;
        }

        User user = new User();

        user.setAddress( toAddress( command ) );
        user.setEmail( command.getEmail() );
        user.setName( command.getName() );
        user.setNotes( command.getNotes() );
        user.setPhoneNumber( command.getPhoneNumber() );
        user.setType( command.getType() );

        user.setStatus( UserStatus.ACTIVE );

        return user;
    }

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setPasswordHash( user.getPasswordHash() );
        userResponse.setAccountLocked( user.getAccountLocked() );
        userResponse.setFailedLoginAttempts( user.getFailedLoginAttempts() );
        userResponse.setFirstLogin( user.getFirstLogin() );
        userResponse.setFullAddress( toFullAddress( user.getAddress() ) );
        userResponse.setProfilePictureUrl( userProfileProfilePicturePath( user ) );
        userResponse.setPreferredLanguage( userProfilePreferredLanguage( user ) );
        userResponse.setRegion( regionToString( userAddressRegion( user ) ) );
        userResponse.setDepartment( userAddressDepartment( user ) );
        userResponse.setDistrict( userAddressDistrict( user ) );
        userResponse.setVillage( userAddressVillage( user ) );
        userResponse.setGpsCoordinates( userAddressGpsCoordinates( user ) );
        userResponse.setCreatedAt( user.getCreatedAt() );
        userResponse.setEmail( user.getEmail() );
        userResponse.setId( user.getId() );
        userResponse.setName( user.getName() );
        userResponse.setPhoneNumber( user.getPhoneNumber() );
        userResponse.setRegistrationNumber( user.getRegistrationNumber() );
        userResponse.setStatus( user.getStatus() );
        userResponse.setType( user.getType() );
        userResponse.setUpdatedAt( user.getUpdatedAt() );
        userResponse.setUserId( user.getUserId() );

        userResponse.setAccountEnabled( user.getStatus() == cm.agribind.usermanagement.enums.UserStatus.ACTIVE );

        afterUserMapping( userResponse, user );

        return userResponse;
    }

    @Override
    public List<UserResponse> toResponseList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserResponse> list = new ArrayList<UserResponse>( users.size() );
        for ( User user : users ) {
            list.add( toResponse( user ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromCommand(UpdateUserCommand command, User user) {
        if ( command == null ) {
            return;
        }

        if ( command != null ) {
            user.setAddress( toAddressFromUpdate( command ) );
        }
        if ( command.getEmail() != null ) {
            user.setEmail( command.getEmail() );
        }
        if ( command.getName() != null ) {
            user.setName( command.getName() );
        }
        if ( command.getNotes() != null ) {
            user.setNotes( command.getNotes() );
        }
        if ( command.getPhoneNumber() != null ) {
            user.setPhoneNumber( command.getPhoneNumber() );
        }
        if ( command.getStatus() != null ) {
            user.setStatus( command.getStatus() );
        }

        afterUpdateMapping( user, command );
    }

    private String userProfileProfilePicturePath(User user) {
        if ( user == null ) {
            return null;
        }
        Profile profile = user.getProfile();
        if ( profile == null ) {
            return null;
        }
        String profilePicturePath = profile.getProfilePicturePath();
        if ( profilePicturePath == null ) {
            return null;
        }
        return profilePicturePath;
    }

    private String userProfilePreferredLanguage(User user) {
        if ( user == null ) {
            return null;
        }
        Profile profile = user.getProfile();
        if ( profile == null ) {
            return null;
        }
        String preferredLanguage = profile.getPreferredLanguage();
        if ( preferredLanguage == null ) {
            return null;
        }
        return preferredLanguage;
    }

    private Region userAddressRegion(User user) {
        if ( user == null ) {
            return null;
        }
        Address address = user.getAddress();
        if ( address == null ) {
            return null;
        }
        Region region = address.getRegion();
        if ( region == null ) {
            return null;
        }
        return region;
    }

    private String userAddressDepartment(User user) {
        if ( user == null ) {
            return null;
        }
        Address address = user.getAddress();
        if ( address == null ) {
            return null;
        }
        String department = address.getDepartment();
        if ( department == null ) {
            return null;
        }
        return department;
    }

    private String userAddressDistrict(User user) {
        if ( user == null ) {
            return null;
        }
        Address address = user.getAddress();
        if ( address == null ) {
            return null;
        }
        String district = address.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district;
    }

    private String userAddressVillage(User user) {
        if ( user == null ) {
            return null;
        }
        Address address = user.getAddress();
        if ( address == null ) {
            return null;
        }
        String village = address.getVillage();
        if ( village == null ) {
            return null;
        }
        return village;
    }

    private String userAddressGpsCoordinates(User user) {
        if ( user == null ) {
            return null;
        }
        Address address = user.getAddress();
        if ( address == null ) {
            return null;
        }
        String gpsCoordinates = address.getGpsCoordinates();
        if ( gpsCoordinates == null ) {
            return null;
        }
        return gpsCoordinates;
    }
}
