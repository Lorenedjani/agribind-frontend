package com.agribind.communication.mapper;

import com.agribind.communication.dto.MessageTemplateDto;
import com.agribind.communication.model.MessageTemplate;
import org.mapstruct.*;

/**
 * MapStruct mapper for MessageTemplate entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    /**
     * Convert MessageTemplate entity to DTO
     */
    MessageTemplateDto toDto(MessageTemplate template);

    /**
     * Convert MessageTemplateDto to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "templateVariables", ignore = true)
    MessageTemplate toEntity(MessageTemplateDto dto);

    /**
     * Update existing MessageTemplate entity from DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromDto(MessageTemplateDto dto, @MappingTarget MessageTemplate template);
}