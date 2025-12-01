package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class AudioMessageResponse {
    private Long id;
    private String title;
    private String language;
    private String audioFileUrl;
    private Integer recipientCount;
    private MessageStatus status;
    private LocalDateTime createdAt;

    // Default constructor
    public AudioMessageResponse() {}

    // Builder pattern constructor
    private AudioMessageResponse(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.language = builder.language;
        this.audioFileUrl = builder.audioFileUrl;
        this.recipientCount = builder.recipientCount;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getAudioFileUrl() { return audioFileUrl; }
    public void setAudioFileUrl(String audioFileUrl) { this.audioFileUrl = audioFileUrl; }

    public Integer getRecipientCount() { return recipientCount; }
    public void setRecipientCount(Integer recipientCount) { this.recipientCount = recipientCount; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder class
    public static class Builder {
        private Long id;
        private String title;
        private String language;
        private String audioFileUrl;
        private Integer recipientCount;
        private MessageStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder audioFileUrl(String audioFileUrl) { this.audioFileUrl = audioFileUrl; return this; }
        public Builder recipientCount(Integer recipientCount) { this.recipientCount = recipientCount; return this; }
        public Builder status(MessageStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AudioMessageResponse build() {
            return new AudioMessageResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}