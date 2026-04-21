package com.agribind.communication.dto;

import java.time.LocalDateTime;

/**
 * Response DTO returned after a government agent successfully publishes
 * an audio announcement.
 */
public class GovernmentAnnouncementResponse {

    private Long          id;
    private String        title;
    private String        language;
    private String        source;          // e.g. "Ministry of Agriculture"
    private String        targetAudience;
    private String        audioUrl;        // relative URL usable by mobile app
    private LocalDateTime publishedAt;
    private String        status;          // "PUBLISHED" | "FAILED"

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public String getTitle()                { return title; }
    public void setTitle(String title)      { this.title = title; }

    public String getLanguage()             { return language; }
    public void setLanguage(String l)       { this.language = l; }

    public String getSource()               { return source; }
    public void setSource(String s)         { this.source = s; }

    public String getTargetAudience()       { return targetAudience; }
    public void setTargetAudience(String a) { this.targetAudience = a; }

    public String getAudioUrl()             { return audioUrl; }
    public void setAudioUrl(String u)       { this.audioUrl = u; }

    public LocalDateTime getPublishedAt()           { return publishedAt; }
    public void setPublishedAt(LocalDateTime t)     { this.publishedAt = t; }

    public String getStatus()               { return status; }
    public void setStatus(String s)         { this.status = s; }
}