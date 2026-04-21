package com.agribind.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request body for government emergency SMS/push alerts.
 */
public class GovernmentAlertRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    /** WEATHER | PEST | MARKET | GENERAL | EMERGENCY */
    @NotBlank
    private String alertType;

    /** NORMAL | HIGH | URGENT | CRITICAL */
    @NotNull
    private String priority;

    /** ALL_MEMBERS | ACTIVE_MEMBERS | DOUALA_ZONE | YAOUNDE_ZONE | CUSTOM */
    @NotBlank
    private String targetAudience;

    private String       specificZone;           // when targetAudience=CUSTOM
    private List<String> channels;               // ["SMS","PUSH"] — default applied in controller

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getTitle()                          { return title; }
    public void   setTitle(String title)              { this.title = title; }

    public String getContent()                        { return content; }
    public void   setContent(String content)          { this.content = content; }

    public String getAlertType()                      { return alertType; }
    public void   setAlertType(String alertType)      { this.alertType = alertType; }

    public String getPriority()                       { return priority; }
    public void   setPriority(String priority)        { this.priority = priority; }

    public String getTargetAudience()                 { return targetAudience; }
    public void   setTargetAudience(String t)         { this.targetAudience = t; }

    public String getSpecificZone()                   { return specificZone; }
    public void   setSpecificZone(String specificZone){ this.specificZone = specificZone; }

    public List<String> getChannels()                 { return channels; }
    public void         setChannels(List<String> c)   { this.channels = c; }
}