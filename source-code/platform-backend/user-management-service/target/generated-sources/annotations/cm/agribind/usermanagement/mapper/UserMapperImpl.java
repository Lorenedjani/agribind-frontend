package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.Profile;
import cm.agribind.usermanagement.entity.User;
import cm.agribind.usermanagement.enums.UserStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-13T09:15:15+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Oracle Corporation)"
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
        user.setType( command.getType() );
        user.setName( command.getName() );
        user.setEmail( command.getEmail() );
        user.setPhoneNumber( command.getPhoneNumber() );
        user.setNotes( command.getNotes() );

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
        userResponse.setId( user.getId() );
        userResponse.setUserId( user.getUserId() );
        userResponse.setType( user.getType() );
        userResponse.setName( user.getName() );
        userResponse.setEmail( user.getEmail() );
        userResponse.setPhoneNumber( user.getPhoneNumber() );
        userResponse.setStatus( user.getStatus() );
        userResponse.setRegistrationNumber( user.getRegistrationNumber() );
        userResponse.setCreatedAt( user.getCreatedAt() );
        userResponse.setUpdatedAt( user.getUpdatedAt() );

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
        if ( command.getName() != null ) {
            user.setName( command.getName() );
        }
        if ( command.getEmail() != null ) {
            user.setEmail( command.getEmail() );
        }
        if ( command.getPhoneNumber() != null ) {
            user.setPhoneNumber( command.getPhoneNumber() );
        }
        if ( command.getStatus() != null ) {
            user.setStatus( command.getStatus() );
        }
        if ( command.getNotes() != null ) {
            user.setNotes( command.getNotes() );
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
}
