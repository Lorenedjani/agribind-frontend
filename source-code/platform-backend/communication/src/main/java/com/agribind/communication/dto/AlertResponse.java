package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class AlertResponse {
    private Long id;
    private String alertId;
    private AlertType type;
    private AlertPriority priority;
    private String title;
    // Stored alert message body/content (what gets sent over SMS, etc.)
    private String content;
    private Integer recipientCount;
    private List<String> channels;
    private Double deliveryRate;
    private AlertStatus status;
    private LocalDateTime createdAt;

    // Default constructor
    public AlertResponse() {}

    // Builder pattern constructor
    private AlertResponse(Builder builder) {
        this.id = builder.id;
        this.alertId = builder.alertId;
        this.type = builder.type;
        this.priority = builder.priority;
        this.title = builder.title;
        this.content = builder.content;
        this.recipientCount = builder.recipientCount;
        this.channels = builder.channels;
        this.deliveryRate = builder.deliveryRate;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public AlertPriority getPriority() { return priority; }
    public void setPriority(AlertPriority priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRecipientCount() { return recipientCount; }
    public void setRecipientCount(Integer recipientCount) { this.recipientCount = recipientCount; }

    public List<String> getChannels() { return channels; }
    public void setChannels(List<String> channels) { this.channels = channels; }

    public Double getDeliveryRate() { return deliveryRate; }
    public void setDeliveryRate(Double deliveryRate) { this.deliveryRate = deliveryRate; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder class
    public static class Builder {
        private Long id;
        private String alertId;
        private AlertType type;
        private AlertPriority priority;
        private String title;
        private String content;
        private Integer recipientCount;
        private List<String> channels;
        private Double deliveryRate;
        private AlertStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder alertId(String alertId) { this.alertId = alertId; return this; }
        public Builder type(AlertType type) { this.type = type; return this; }
        public Builder priority(AlertPriority priority) { this.priority = priority; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder recipientCount(Integer recipientCount) { this.recipientCount = recipientCount; return this; }
        public Builder channels(List<String> channels) { this.channels = channels; return this; }
        public Builder deliveryRate(Double deliveryRate) { this.deliveryRate = deliveryRate; return this; }
        public Builder status(AlertStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AlertResponse build() {
            return new AlertResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}