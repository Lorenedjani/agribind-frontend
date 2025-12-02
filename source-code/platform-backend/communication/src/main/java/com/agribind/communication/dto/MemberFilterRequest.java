package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class MemberFilterRequest {
    private TargetAudience audience;
    private String zone;
    private Boolean activeOnly;
    private List<Long> memberIds;

    // Default constructor
    public MemberFilterRequest() {}

    // All arguments constructor
    public MemberFilterRequest(TargetAudience audience, String zone, Boolean activeOnly, List<Long> memberIds) {
        this.audience = audience;
        this.zone = zone;
        this.activeOnly = activeOnly;
        this.memberIds = memberIds;
    }

    // Getters and Setters
    public TargetAudience getAudience() { return audience; }
    public void setAudience(TargetAudience audience) { this.audience = audience; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public Boolean getActiveOnly() { return activeOnly; }
    public void setActiveOnly(Boolean activeOnly) { this.activeOnly = activeOnly; }

    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }

    // Builder class
    public static class Builder {
        private TargetAudience audience;
        private String zone;
        private Boolean activeOnly;
        private List<Long> memberIds;

        public Builder audience(TargetAudience audience) { this.audience = audience; return this; }
        public Builder zone(String zone) { this.zone = zone; return this; }
        public Builder activeOnly(Boolean activeOnly) { this.activeOnly = activeOnly; return this; }
        public Builder memberIds(List<Long> memberIds) { this.memberIds = memberIds; return this; }

        public MemberFilterRequest build() {
            return new MemberFilterRequest(audience, zone, activeOnly, memberIds);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}