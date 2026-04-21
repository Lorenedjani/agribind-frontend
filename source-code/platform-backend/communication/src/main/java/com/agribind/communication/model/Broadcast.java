package com.agribind.communication.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "broadcasts")
public class Broadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // Announcement type as a string (UI uses values like announcement/need/payment/etc.)
    private String type;

    @Enumerated(EnumType.STRING)
    private TargetAudience targetAudience;

    private String repeatSchedule;

    private LocalDateTime scheduleFor;
    private LocalDateTime expiresAt;

    private boolean autoPlay;
    private boolean smsTranscription;
    private boolean emailEnabled;

    private String scriptTemplateId;

    // UI status: sent|draft|scheduled|failed (we currently set sent|scheduled)
    private String status;
    private Integer listeners;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "broadcast", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BroadcastVariant> variants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (listeners == null) listeners = 0;
        if (status == null) status = "sent";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public TargetAudience getTargetAudience() { return targetAudience; }
    public void setTargetAudience(TargetAudience targetAudience) { this.targetAudience = targetAudience; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getListeners() { return listeners; }
    public void setListeners(Integer listeners) { this.listeners = listeners; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<BroadcastVariant> getVariants() { return variants; }
    public void setVariants(List<BroadcastVariant> variants) { this.variants = variants; }
}

