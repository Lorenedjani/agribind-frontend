package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class SmsMessageRequest {
    @NotNull
    private TargetAudience targetAudience;

    private String specificZone;

    @NotBlank
    @Size(max = 160, message = "SMS content must not exceed 160 characters")
    private String content;

    private MessagePriority priority = MessagePriority.NORMAL;

    private LocalDateTime scheduledAt;

    private Long templateId;

    // Default constructor
    public SmsMessageRequest() {}

    // All arguments constructor
    public SmsMessageRequest(TargetAudience targetAudience, String specificZone, String content,
                            MessagePriority priority, LocalDateTime scheduledAt, Long templateId) {
        this.targetAudience = targetAudience;
        this.specificZone = specificZone;
        this.content = content;
        this.priority = priority;
        this.scheduledAt = scheduledAt;
        this.templateId = templateId;
    }

    // Getters and Setters
    public TargetAudience getTargetAudience() { return targetAudience; }
    public void setTargetAudience(TargetAudience targetAudience) { this.targetAudience = targetAudience; }

    public String getSpecificZone() { return specificZone; }
    public void setSpecificZone(String specificZone) { this.specificZone = specificZone; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessagePriority getPriority() { return priority; }
    public void setPriority(MessagePriority priority) { this.priority = priority; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    // Builder class
    public static class Builder {
        private TargetAudience targetAudience;
        private String specificZone;
        private String content;
        private MessagePriority priority = MessagePriority.NORMAL;
        private LocalDateTime scheduledAt;
        private Long templateId;

        public Builder targetAudience(TargetAudience targetAudience) { this.targetAudience = targetAudience; return this; }
        public Builder specificZone(String specificZone) { this.specificZone = specificZone; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder priority(MessagePriority priority) { this.priority = priority; return this; }
        public Builder scheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; return this; }
        public Builder templateId(Long templateId) { this.templateId = templateId; return this; }

        public SmsMessageRequest build() {
            return new SmsMessageRequest(targetAudience, specificZone, content, priority, scheduledAt, templateId);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}