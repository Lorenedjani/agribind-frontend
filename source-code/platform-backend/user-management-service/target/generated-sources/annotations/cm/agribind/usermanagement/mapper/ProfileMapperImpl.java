package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.UpdateProfileCommand;
import cm.agribind.usermanagement.entity.Profile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
<<<<<<< HEAD
    date = "2026-01-06T15:21:17+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
=======
    date = "2026-01-09T06:34:27+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Amazon.com Inc.)"
>>>>>>> e5d3c9b455daadf9a49a3e40cd8bda3f862c66a0
)
@Component
public class ProfileMapperImpl implements ProfileMapper {

    @Override
    public Profile toEntity(UpdateProfileCommand command) {
        if ( command == null ) {
            return null;
        }

        Profile profile = new Profile();

        profile.setBio( command.getBio() );
        profile.setPreferredLanguage( command.getPreferredLanguage() );
        profile.setReceiveSmsNotifications( command.getReceiveSmsNotifications() );
        profile.setReceiveEmailNotifications( command.getReceiveEmailNotifications() );
        profile.setReceivePushNotifications( command.getReceivePushNotifications() );
        profile.setSkills( command.getSkills() );
        profile.setDateOfBirth( command.getDateOfBirth() );
        profile.setGender( command.getGender() );
        profile.setMaritalStatus( command.getMaritalStatus() );
        profile.setDependentsCount( command.getDependentsCount() );

        return profile;
    }

    @Override
    public void updateEntityFromCommand(UpdateProfileCommand command, Profile profile) {
        if ( command == null ) {
            return;
        }

        if ( command.getBio() != null ) {
            profile.setBio( command.getBio() );
        }
        if ( command.getPreferredLanguage() != null ) {
            profile.setPreferredLanguage( command.getPreferredLanguage() );
        }
        if ( command.getReceiveSmsNotifications() != null ) {
            profile.setReceiveSmsNotifications( command.getReceiveSmsNotifications() );
        }
        if ( command.getReceiveEmailNotifications() != null ) {
            profile.setReceiveEmailNotifications( command.getReceiveEmailNotifications() );
        }
        if ( command.getReceivePushNotifications() != null ) {
            profile.setReceivePushNotifications( command.getReceivePushNotifications() );
        }
        if ( command.getSkills() != null ) {
            profile.setSkills( command.getSkills() );
        }
        if ( command.getDateOfBirth() != null ) {
            profile.setDateOfBirth( command.getDateOfBirth() );
        }
        if ( command.getGender() != null ) {
            profile.setGender( command.getGender() );
        }
        if ( command.getMaritalStatus() != null ) {
            profile.setMaritalStatus( command.getMaritalStatus() );
        }
        if ( command.getDependentsCount() != null ) {
            profile.setDependentsCount( command.getDependentsCount() );
        }
    }
}
