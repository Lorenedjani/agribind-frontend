package com.agribind.communication.mapper;

import com.agribind.communication.dto.SmsMessageRequest;
import com.agribind.communication.dto.SmsMessageResponse;
import com.agribind.communication.model.Message;
import com.agribind.communication.model.MessageStatus;
import com.agribind.communication.model.MessageType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manual mapper for Message entity and DTOs
 */
@Component
public class MessageMapper {

    /**
     * Convert Message entity to SmsMessageResponse
     * Maps only the fields needed for SMS response
     */
    public SmsMessageResponse toSmsResponse(Message message) {
        if (message == null) {
            return null;
        }

        SmsMessageResponse response = new SmsMessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setRecipientCount(message.getTotalRecipients());
        response.setEstimatedCost(message.getEstimatedCost());
        response.setStatus(message.getStatus());
        response.setCreatedAt(message.getCreatedAt());

        return response;
    }

    /**
     * Convert Message entity to SmsMessageResponse using Builder pattern
     */
    public SmsMessageResponse toSmsResponseWithBuilder(Message message) {
        if (message == null) {
            return null;
        }

        return SmsMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .recipientCount(message.getTotalRecipients())
                .estimatedCost(message.getEstimatedCost())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Convert SmsMessageRequest to Message entity for SMS
     * Note: Many fields are not in the request and should be set separately
     */
    public Message toEntity(SmsMessageRequest request) {
        if (request == null) {
            return null;
        }

        Message message = new Message();

        // Set fields from request
        message.setTargetAudience(request.getTargetAudience());
        message.setSpecificZone(request.getSpecificZone());
        message.setContent(request.getContent());
        message.setPriority(request.getPriority());
        message.setScheduledAt(request.getScheduledAt());

        // Set SMS-specific defaults
        message.setType(MessageType.SMS);
        message.setChannels(List.of("SMS"));

        // Other fields will be set by @PrePersist or service logic:
        // - id (auto-generated)
        // - status (@PrePersist sets to DRAFT)
        // - totalRecipients, deliveredCount, failedCount (business logic)
        // - estimatedCost, actualCost (business logic)
        // - createdBy (should be set by service/authentication)
        // - createdAt (@PrePersist)
        // - sentAt (when actually sent)

        return message;
    }

    /**
     * Convert SmsMessageRequest to Message entity with additional context
     * Includes estimated values from business logic
     */
    public Message toEntityWithContext(SmsMessageRequest request,
                                      String createdBy,
                                      Integer estimatedRecipientCount,
                                      Double estimatedCost) {
        if (request == null) {
            return null;
        }

        Message message = toEntity(request);

        // Set context from service layer
        message.setCreatedBy(createdBy);
        message.setTotalRecipients(estimatedRecipientCount != null ? estimatedRecipientCount : 0);
        message.setEstimatedCost(estimatedCost != null ? estimatedCost : 0.0);

        return message;
    }

    /**
     * Update existing Message entity from SmsMessageRequest DTO
     * Only updates fields that can be changed after creation
     */
    public void updateEntityFromDto(SmsMessageRequest dto, Message message) {
        if (dto == null || message == null) {
            return;
        }

        // Only update fields that should be updatable
        // Do NOT update: id, type, createdBy, createdAt, sentAt, deliveredCount, failedCount, actualCost

        // Update fields from DTO
        message.setTargetAudience(dto.getTargetAudience());
        message.setSpecificZone(dto.getSpecificZone());
        message.setContent(dto.getContent());
        message.setPriority(dto.getPriority());
        message.setScheduledAt(dto.getScheduledAt());

        // Note: templateId in request is for reference, not directly mapped to Message entity
        // This should be handled by service layer
    }

    /**
     * Convert Message entity to full response (more fields than SMS response)
     * Useful for admin interfaces or detailed views
     */
    public SmsMessageResponse toDetailedResponse(Message message) {
        if (message == null) {
            return null;
        }

        return SmsMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .recipientCount(message.getTotalRecipients())
                .estimatedCost(message.getEstimatedCost())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();
        // Note: Could extend SmsMessageResponse or create a new DetailedMessageResponse
        // if more fields from Message entity are needed
    }

    /**
     * Convert list of Message entities to list of SmsMessageResponse
     */
    public List<SmsMessageResponse> toSmsResponseList(List<Message> messages) {
        if (messages == null) {
            return List.of();
        }

        return messages.stream()
                .map(this::toSmsResponse)
                .toList();
    }

    /**
     * Create SMS message entity from template content
     * Useful when using message templates
     */
    public Message createSmsFromTemplate(String templateContent,
                                        SmsMessageRequest request,
                                        String createdBy) {
        if (templateContent == null || request == null) {
            return null;
        }

        Message message = toEntity(request);
        message.setContent(templateContent); // Override with template content
        message.setCreatedBy(createdBy);
        message.setType(MessageType.SMS);

        return message;
    }

    /**
     * Create a summary SMS response (minimal fields)
     * Useful for listing or quick status checks
     */
    public SmsMessageResponse toSummaryResponse(Message message) {
        if (message == null) {
            return null;
        }

        return SmsMessageResponse.builder()
                .id(message.getId())
                .content(truncateContent(message.getContent(), 50)) // Truncate long content
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Helper method to truncate content for summary views
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * Update message status and sent timestamp
     * Separate from DTO update as this is business logic
     */
    public void updateMessageStatus(Message message, MessageStatus newStatus) {
        if (message == null) {
            return;
        }

        message.setStatus(newStatus);

        if (newStatus == MessageStatus.SENT || newStatus == MessageStatus.SENDING) {
            message.setSentAt(LocalDateTime.now());
        }
    }

    /**
     * Update delivery statistics
     * Should be called when delivery reports are received
     */
    public void updateDeliveryStats(Message message, Integer deliveredCount, Integer failedCount) {
        if (message == null) {
            return;
        }

        message.setDeliveredCount(deliveredCount != null ? deliveredCount : 0);
        message.setFailedCount(failedCount != null ? failedCount : 0);

        // Update status based on delivery stats
        if (deliveredCount != null && message.getTotalRecipients() != null &&
            deliveredCount.equals(message.getTotalRecipients())) {
            message.setStatus(MessageStatus.SENT);
        } else if (deliveredCount != null && deliveredCount > 0) {
            message.setStatus(MessageStatus.PARTIALLY_SENT);
        }
    }
}