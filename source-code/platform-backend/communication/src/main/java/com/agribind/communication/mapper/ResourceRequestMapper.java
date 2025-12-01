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
     * Convert ResourceRequest entity to DTO
     */
    ResourceRequestDto toDto(ResourceRequest request);

    /**
     * Convert ResourceRequestDto to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestDate", expression = "java(java.time.LocalDateTime.now())")
    ResourceRequest toEntity(ResourceRequestDto dto);

    /**
     * Convert ResourceRequest entity to Response DTO
     */
    ResourceRequestResponse toResponse(ResourceRequest request);

    /**
     * Update existing ResourceRequest entity from DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestDate", ignore = true)
    void updateEntityFromDto(ResourceRequestDto dto, @MappingTarget ResourceRequest request);
}