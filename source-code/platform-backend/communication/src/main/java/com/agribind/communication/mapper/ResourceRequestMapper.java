package com.agribind.communication.mapper;

import com.agribind.communication.dto.ResourceRequestDto;
import com.agribind.communication.dto.ResourceRequestResponse;
import com.agribind.communication.model.ResourceRequest;
import org.mapstruct.*;

/**
 * MapStruct mapper for ResourceRequest entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResourceRequestMapper {

    /**
     * Convert ResourceRequest entity to ResourceRequestResponse
     */
    @Mapping(source = "id", target = "requestId")
    @Mapping(source = "createdAt", target = "requestDate")
    ResourceRequestResponse toResourceRequestResponse(ResourceRequest resourceRequest);

    /**
     * Convert ResourceRequestDto to ResourceRequest entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    ResourceRequest toEntity(ResourceRequestDto dto);

    /**
     * Update existing ResourceRequest entity from DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromDto(ResourceRequestDto dto, @MappingTarget ResourceRequest resourceRequest);
}