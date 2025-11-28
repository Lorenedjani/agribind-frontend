package com.agribind.announcement.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AudioMessageDTO {
    private Long id;
    private String language;
    private String languageDisplayName;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private Integer duration;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private Integer playCount;

     // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getLanguageDisplayName() { return languageDisplayName; }
    public void setLanguageDisplayName(String languageDisplayName) { this.languageDisplayName = languageDisplayName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Integer getPlayCount() { return playCount; }
    public void setPlayCount(Integer playCount) { this.playCount = playCount; }
}
