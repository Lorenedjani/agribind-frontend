package com.agribind.communication.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audio_library_items")
public class AudioLibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String language;

    // Duration can be provided by the frontend (best-effort). If not provided, keep it null/0.
    private Integer durationSeconds;
    private String durationLabel;

    private String fileName;

    // Server-served path, e.g. "/audio/<filename>"
    private String filePath;

    private Integer usageCount;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        usageCount = usageCount == null ? 0 : usageCount;
        if (durationSeconds == null) durationSeconds = 0;
        if (durationLabel == null) durationLabel = "0:00";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getDurationLabel() { return durationLabel; }
    public void setDurationLabel(String durationLabel) { this.durationLabel = durationLabel; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

