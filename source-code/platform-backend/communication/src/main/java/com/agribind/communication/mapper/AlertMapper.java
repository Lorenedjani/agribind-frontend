package com.agribind.communication.mapper;

import com.agribind.communication.dto.AlertResponse;
import com.agribind.communication.model.Alert;
import org.mapstruct.*;

/**
 * MapStruct mapper for Alert entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlertMapper {

    /**
     * Convert Alert entity to AlertResponse
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "alertId", source = "alertId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "recipientCount", source = "recipientCount")
    @Mapping(target = "channels", source = "channels")
    @Mapping(target = "deliveryRate", source = "deliveryRate")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    AlertResponse toResponse(Alert alert);

    /**
     * Convert AlertRequest to Alert entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alertId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "channels", source = "channels")
    Alert toEntity(com.agribind.communication.dto.AlertRequest request);

    /**
     * Update existing Alert entity from DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alertId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(com.agribind.communication.dto.AlertRequest dto, @MappingTarget Alert alert);
}