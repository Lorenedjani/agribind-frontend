package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class SmsMessageResponse {
    private Long id;
    private String content;
    private Integer recipientCount;
    private Double estimatedCost;
    private MessageStatus status;
    private LocalDateTime createdAt;

    // Default constructor
    public SmsMessageResponse() {}

    // Builder pattern constructor
    private SmsMessageResponse(Builder builder) {
        this.id = builder.id;
        this.content = builder.content;
        this.recipientCount = builder.recipientCount;
        this.estimatedCost = builder.estimatedCost;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRecipientCount() { return recipientCount; }
    public void setRecipientCount(Integer recipientCount) { this.recipientCount = recipientCount; }

    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder class
    public static class Builder {
        private Long id;
        private String content;
        private Integer recipientCount;
        private Double estimatedCost;
        private MessageStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder recipientCount(Integer recipientCount) { this.recipientCount = recipientCount; return this; }
        public Builder estimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; return this; }
        public Builder status(MessageStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SmsMessageResponse build() {
            return new SmsMessageResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}