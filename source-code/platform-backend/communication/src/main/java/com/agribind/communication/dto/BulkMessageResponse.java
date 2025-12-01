package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class BulkMessageResponse {
    private Long messageId;
    private Integer recipientCount;
    private Double estimatedCost;
    private MessageStatus status;
    private String message;

    // Default constructor
    public BulkMessageResponse() {}

    // Builder pattern constructor
    private BulkMessageResponse(Builder builder) {
        this.messageId = builder.messageId;
        this.recipientCount = builder.recipientCount;
        this.estimatedCost = builder.estimatedCost;
        this.status = builder.status;
        this.message = builder.message;
    }

    // Getters and Setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Integer getRecipientCount() { return recipientCount; }
    public void setRecipientCount(Integer recipientCount) { this.recipientCount = recipientCount; }

    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // Builder class
    public static class Builder {
        private Long messageId;
        private Integer recipientCount;
        private Double estimatedCost;
        private MessageStatus status;
        private String message;

        public Builder messageId(Long messageId) { this.messageId = messageId; return this; }
        public Builder recipientCount(Integer recipientCount) { this.recipientCount = recipientCount; return this; }
        public Builder estimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; return this; }
        public Builder status(MessageStatus status) { this.status = status; return this; }
        public Builder message(String message) { this.message = message; return this; }

        public BulkMessageResponse build() {
            return new BulkMessageResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}