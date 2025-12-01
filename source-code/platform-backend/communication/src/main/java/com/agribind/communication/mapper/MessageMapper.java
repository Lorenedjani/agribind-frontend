package com.agribind.communication.mapper;

import com.agribind.communication.dto.SmsMessageRequest;
import com.agribind.communication.dto.SmsMessageResponse;
import com.agribind.communication.model.Message;
import org.mapstruct.*;

import java.time.LocalDateTime;

/**
 * MapStruct mapper for Message entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    /**
     * Convert Message entity to SmsMessageResponse
     */
    @Mapping(source = "id", target = "messageId")
    @Mapping(source = "createdAt", target = "sentAt")
    SmsMessageResponse toSmsMessageResponse(Message message);

    /**
     * Convert SmsMessageRequest to Message entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "messageChannels", ignore = true)
    Message toEntity(SmsMessageRequest request);

    /**
     * Update existing Message entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromRequest(SmsMessageRequest request, @MappingTarget Message message);
}