package cm.agribind.usermanagement.mapper;

import cm.agribind.usermanagement.dto.command.UpdateProfileCommand;
import cm.agribind.usermanagement.entity.Profile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "profilePicturePath", ignore = true)
    Profile toEntity(UpdateProfileCommand command);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "profilePicturePath", ignore = true)
    void updateEntityFromCommand(UpdateProfileCommand command, @MappingTarget Profile profile);
}