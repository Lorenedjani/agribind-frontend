package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class AudioMessageRequest {
    @NotNull
    private TargetAudience targetAudience;

    private String specificZone;

    @NotBlank
    private String title;

    @NotBlank
    private String language;

    private Boolean autoPlay = false;

    private MessagePriority priority = MessagePriority.NORMAL;

    // Default constructor
    public AudioMessageRequest() {}

    // All arguments constructor
    public AudioMessageRequest(TargetAudience targetAudience, String specificZone, String title,
                              String language, Boolean autoPlay, MessagePriority priority) {
        this.targetAudience = targetAudience;
        this.specificZone = specificZone;
        this.title = title;
        this.language = language;
        this.autoPlay = autoPlay;
        this.priority = priority;
    }

    // Getters and Setters
    public TargetAudience getTargetAudience() { return targetAudience; }
    public void setTargetAudience(TargetAudience targetAudience) { this.targetAudience = targetAudience; }

    public String getSpecificZone() { return specificZone; }
    public void setSpecificZone(String specificZone) { this.specificZone = specificZone; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getAutoPlay() { return autoPlay; }
    public void setAutoPlay(Boolean autoPlay) { this.autoPlay = autoPlay; }

    public MessagePriority getPriority() { return priority; }
    public void setPriority(MessagePriority priority) { this.priority = priority; }
}