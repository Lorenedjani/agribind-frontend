package com.agribind.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class BroadcastRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String type;

    @NotBlank
    private String targetAudience;

    private String repeatSchedule;

    private LocalDateTime scheduleFor;
    private LocalDateTime expiresAt;

    private boolean autoPlay;
    private boolean smsTranscription;
    private boolean emailEnabled;

    private String scriptTemplateId;

    @NotNull
    private List<BroadcastVariantRequestDto> variants;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getRepeatSchedule() { return repeatSchedule; }
    public void setRepeatSchedule(String repeatSchedule) { this.repeatSchedule = repeatSchedule; }

    public LocalDateTime getScheduleFor() { return scheduleFor; }
    public void setScheduleFor(LocalDateTime scheduleFor) { this.scheduleFor = scheduleFor; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isAutoPlay() { return autoPlay; }
    public void setAutoPlay(boolean autoPlay) { this.autoPlay = autoPlay; }

    public boolean isSmsTranscription() { return smsTranscription; }
    public void setSmsTranscription(boolean smsTranscription) { this.smsTranscription = smsTranscription; }

    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

    public String getScriptTemplateId() { return scriptTemplateId; }
    public void setScriptTemplateId(String scriptTemplateId) { this.scriptTemplateId = scriptTemplateId; }

    public List<BroadcastVariantRequestDto> getVariants() { return variants; }
    public void setVariants(List<BroadcastVariantRequestDto> variants) { this.variants = variants; }
}

