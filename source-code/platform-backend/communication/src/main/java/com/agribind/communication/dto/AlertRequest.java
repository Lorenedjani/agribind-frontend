package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class AlertRequest {
    @NotNull
    private AlertType type;

    @NotNull
    private AlertPriority priority;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotEmpty
    private List<String> channels;

    @NotNull
    private TargetAudience targetAudience;

    private String specificZone;

    // Default constructor
    public AlertRequest() {}

    // All arguments constructor
    public AlertRequest(AlertType type, AlertPriority priority, String title, String content,
                       List<String> channels, TargetAudience targetAudience, String specificZone) {
        this.type = type;
        this.priority = priority;
        this.title = title;
        this.content = content;
        this.channels = channels;
        this.targetAudience = targetAudience;
        this.specificZone = specificZone;
    }

    // Getters and Setters
    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public AlertPriority getPriority() { return priority; }
    public void setPriority(AlertPriority priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getChannels() { return channels; }
    public void setChannels(List<String> channels) { this.channels = channels; }

    public TargetAudience getTargetAudience() { return targetAudience; }
    public void setTargetAudience(TargetAudience targetAudience) { this.targetAudience = targetAudience; }

    public String getSpecificZone() { return specificZone; }
    public void setSpecificZone(String specificZone) { this.specificZone = specificZone; }
}