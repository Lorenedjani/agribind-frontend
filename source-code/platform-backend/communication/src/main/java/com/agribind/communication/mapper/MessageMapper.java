package com.agribind.communication.mapper;

import com.agribind.communication.dto.SmsMessageResponse;
import com.agribind.communication.model.Message;
import org.mapstruct.*;

/**
 * MapStruct mapper for Message entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    /**
     * Convert Message entity to SmsMessageResponse
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "recipientCount", source = "totalRecipients")
    @Mapping(target = "estimatedCost", source = "estimatedCost")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    SmsMessageResponse toSmsResponse(Message message);

    /**
     * Convert SmsMessageRequest to Message entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "channels", ignore = true)
    Message toEntity(com.agribind.communication.dto.SmsMessageRequest request);

    /**
     * Update existing Message entity from DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(com.agribind.communication.dto.SmsMessageRequest dto, @MappingTarget Message message);
}