package com.agribind.communication.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BroadcastDto {
    private Long id;
    private String title;
    private String type;

    private List<BroadcastVariantDto> variants;

    private Integer duration;
    private String durationLabel;

    private Integer listeners;
    private LocalDateTime date;
    private LocalDateTime expiresAt;

    private String status;
    private String repeatSchedule;
    private String targetAudience;

    private boolean autoPlay;
    private boolean smsTranscription;
    private boolean emailEnabled;

    private String scriptTemplateId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<BroadcastVariantDto> getVariants() { return variants; }
    public void setVariants(List<BroadcastVariantDto> variants) { this.variants = variants; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getDurationLabel() { return durationLabel; }
    public void setDurationLabel(String durationLabel) { this.durationLabel = durationLabel; }

    public Integer getListeners() { return listeners; }
    public void setListeners(Integer listeners) { this.listeners = listeners; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRepeatSchedule() { return repeatSchedule; }
    public void setRepeatSchedule(String repeatSchedule) { this.repeatSchedule = repeatSchedule; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public boolean isAutoPlay() { return autoPlay; }
    public void setAutoPlay(boolean autoPlay) { this.autoPlay = autoPlay; }

    public boolean isSmsTranscription() { return smsTranscription; }
    public void setSmsTranscription(boolean smsTranscription) { this.smsTranscription = smsTranscription; }

    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

    public String getScriptTemplateId() { return scriptTemplateId; }
    public void setScriptTemplateId(String scriptTemplateId) { this.scriptTemplateId = scriptTemplateId; }
}

