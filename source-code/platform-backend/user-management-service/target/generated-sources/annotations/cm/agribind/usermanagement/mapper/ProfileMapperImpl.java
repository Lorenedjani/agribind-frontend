package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.UpdateProfileCommand;
import cm.agribind.usermanagement.entity.Profile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-10T02:43:18+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
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
        profile.setDateOfBirth( command.getDateOfBirth() );
        profile.setDependentsCount( command.getDependentsCount() );
        profile.setGender( command.getGender() );
        profile.setMaritalStatus( command.getMaritalStatus() );
        profile.setPreferredLanguage( command.getPreferredLanguage() );
        profile.setReceiveEmailNotifications( command.getReceiveEmailNotifications() );
        profile.setReceivePushNotifications( command.getReceivePushNotifications() );
        profile.setReceiveSmsNotifications( command.getReceiveSmsNotifications() );
        profile.setSkills( command.getSkills() );

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
        if ( command.getDateOfBirth() != null ) {
            profile.setDateOfBirth( command.getDateOfBirth() );
        }
        if ( command.getDependentsCount() != null ) {
            profile.setDependentsCount( command.getDependentsCount() );
        }
        if ( command.getGender() != null ) {
            profile.setGender( command.getGender() );
        }
        if ( command.getMaritalStatus() != null ) {
            profile.setMaritalStatus( command.getMaritalStatus() );
        }
        if ( command.getPreferredLanguage() != null ) {
            profile.setPreferredLanguage( command.getPreferredLanguage() );
        }
        if ( command.getReceiveEmailNotifications() != null ) {
            profile.setReceiveEmailNotifications( command.getReceiveEmailNotifications() );
        }
        if ( command.getReceivePushNotifications() != null ) {
            profile.setReceivePushNotifications( command.getReceivePushNotifications() );
        }
        if ( command.getReceiveSmsNotifications() != null ) {
            profile.setReceiveSmsNotifications( command.getReceiveSmsNotifications() );
        }
        if ( command.getSkills() != null ) {
            profile.setSkills( command.getSkills() );
        }
    }
}
