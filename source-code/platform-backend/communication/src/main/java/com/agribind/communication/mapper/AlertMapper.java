package com.agribind.communication.mapper;

import com.agribind.communication.dto.AlertRequest;
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
    @Mapping(source = "id", target = "alertId")
    @Mapping(source = "createdAt", target = "createdAt")
    AlertResponse toAlertResponse(Alert alert);

    /**
     * Convert AlertRequest to Alert entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "alertChannels", ignore = true)
    Alert toEntity(AlertRequest request);

    /**
     * Update existing Alert entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromRequest(AlertRequest request, @MappingTarget Alert alert);
}