package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class BulkMessageRequest {
    @NotNull
    private TargetAudience recipientAudience;

    private String specificZone;

    private Long templateId;

    @NotBlank
    private String messageContent;

    private MessagePriority priority = MessagePriority.NORMAL;

    private LocalDateTime scheduledDelivery;

    // Default constructor
    public BulkMessageRequest() {}

    // All arguments constructor
    public BulkMessageRequest(TargetAudience recipientAudience, String specificZone, Long templateId,
                             String messageContent, MessagePriority priority, LocalDateTime scheduledDelivery) {
        this.recipientAudience = recipientAudience;
        this.specificZone = specificZone;
        this.templateId = templateId;
        this.messageContent = messageContent;
        this.priority = priority;
        this.scheduledDelivery = scheduledDelivery;
    }

    // Getters and Setters
    public TargetAudience getRecipientAudience() { return recipientAudience; }
    public void setRecipientAudience(TargetAudience recipientAudience) { this.recipientAudience = recipientAudience; }

    public String getSpecificZone() { return specificZone; }
    public void setSpecificZone(String specificZone) { this.specificZone = specificZone; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public MessagePriority getPriority() { return priority; }
    public void setPriority(MessagePriority priority) { this.priority = priority; }

    public LocalDateTime getScheduledDelivery() { return scheduledDelivery; }
    public void setScheduledDelivery(LocalDateTime scheduledDelivery) { this.scheduledDelivery = scheduledDelivery; }
}