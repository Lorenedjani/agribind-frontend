package com.agribind.announcement.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnnouncementDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String priority;
    private Long cooperativeId;
    private String targetAudience;
    private LocalDateTime publishDate;
    private LocalDateTime expirationDate;
    private String status;
    private List<AudioMessageDTO> audioMessages;
    private Integer viewCount;
    private Integer downloadCount;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Long getCooperativeId() { return cooperativeId; }
    public void setCooperativeId(Long cooperativeId) { this.cooperativeId = cooperativeId; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public LocalDateTime getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDateTime publishDate) { this.publishDate = publishDate; }

    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { this.expirationDate = expirationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public List<AudioMessageDTO> getAudioMessages() { return audioMessages; }
    public void setAudioMessages(List<AudioMessageDTO> audioMessages) { this.audioMessages = audioMessages; }
}
