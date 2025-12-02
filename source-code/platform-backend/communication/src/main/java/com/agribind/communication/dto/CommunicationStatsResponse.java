package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class CommunicationStatsResponse {
    private Integer totalMessagesSent;
    private Integer messagesSentThisWeek;
    private Integer audioMessagesTotal;
    private Integer audioLanguagesSupported;
    private Integer activeAlerts;
    private Integer criticalAlerts;
    private Double deliveryRate;
    private Double deliveryRateChange;
    private Integer activeMembers;
    private Double activeLoansAmount;
    private Integer lowStockAlerts;
    private LocalDateTime lastUpdated;

    // Default constructor
    public CommunicationStatsResponse() {}

    // Builder pattern constructor
    private CommunicationStatsResponse(Builder builder) {
        this.totalMessagesSent = builder.totalMessagesSent;
        this.messagesSentThisWeek = builder.messagesSentThisWeek;
        this.audioMessagesTotal = builder.audioMessagesTotal;
        this.audioLanguagesSupported = builder.audioLanguagesSupported;
        this.activeAlerts = builder.activeAlerts;
        this.criticalAlerts = builder.criticalAlerts;
        this.deliveryRate = builder.deliveryRate;
        this.deliveryRateChange = builder.deliveryRateChange;
        this.activeMembers = builder.activeMembers;
        this.activeLoansAmount = builder.activeLoansAmount;
        this.lowStockAlerts = builder.lowStockAlerts;
        this.lastUpdated = builder.lastUpdated;
    }

    // Getters and Setters
    public Integer getTotalMessagesSent() { return totalMessagesSent; }
    public void setTotalMessagesSent(Integer totalMessagesSent) { this.totalMessagesSent = totalMessagesSent; }

    public Integer getMessagesSentThisWeek() { return messagesSentThisWeek; }
    public void setMessagesSentThisWeek(Integer messagesSentThisWeek) { this.messagesSentThisWeek = messagesSentThisWeek; }

    public Integer getAudioMessagesTotal() { return audioMessagesTotal; }
    public void setAudioMessagesTotal(Integer audioMessagesTotal) { this.audioMessagesTotal = audioMessagesTotal; }

    public Integer getAudioLanguagesSupported() { return audioLanguagesSupported; }
    public void setAudioLanguagesSupported(Integer audioLanguagesSupported) { this.audioLanguagesSupported = audioLanguagesSupported; }

    public Integer getActiveAlerts() { return activeAlerts; }
    public void setActiveAlerts(Integer activeAlerts) { this.activeAlerts = activeAlerts; }

    public Integer getCriticalAlerts() { return criticalAlerts; }
    public void setCriticalAlerts(Integer criticalAlerts) { this.criticalAlerts = criticalAlerts; }

    public Double getDeliveryRate() { return deliveryRate; }
    public void setDeliveryRate(Double deliveryRate) { this.deliveryRate = deliveryRate; }

    public Double getDeliveryRateChange() { return deliveryRateChange; }
    public void setDeliveryRateChange(Double deliveryRateChange) { this.deliveryRateChange = deliveryRateChange; }

    public Integer getActiveMembers() { return activeMembers; }
    public void setActiveMembers(Integer activeMembers) { this.activeMembers = activeMembers; }

    public Double getActiveLoansAmount() { return activeLoansAmount; }
    public void setActiveLoansAmount(Double activeLoansAmount) { this.activeLoansAmount = activeLoansAmount; }

    public Integer getLowStockAlerts() { return lowStockAlerts; }
    public void setLowStockAlerts(Integer lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    // Builder class
    public static class Builder {
        private Integer totalMessagesSent;
        private Integer messagesSentThisWeek;
        private Integer audioMessagesTotal;
        private Integer audioLanguagesSupported;
        private Integer activeAlerts;
        private Integer criticalAlerts;
        private Double deliveryRate;
        private Double deliveryRateChange;
        private Integer activeMembers;
        private Double activeLoansAmount;
        private Integer lowStockAlerts;
        private LocalDateTime lastUpdated;

        public Builder totalMessagesSent(Integer totalMessagesSent) { this.totalMessagesSent = totalMessagesSent; return this; }
        public Builder messagesSentThisWeek(Integer messagesSentThisWeek) { this.messagesSentThisWeek = messagesSentThisWeek; return this; }
        public Builder audioMessagesTotal(Integer audioMessagesTotal) { this.audioMessagesTotal = audioMessagesTotal; return this; }
        public Builder audioLanguagesSupported(Integer audioLanguagesSupported) { this.audioLanguagesSupported = audioLanguagesSupported; return this; }
        public Builder activeAlerts(Integer activeAlerts) { this.activeAlerts = activeAlerts; return this; }
        public Builder criticalAlerts(Integer criticalAlerts) { this.criticalAlerts = criticalAlerts; return this; }
        public Builder deliveryRate(Double deliveryRate) { this.deliveryRate = deliveryRate; return this; }
        public Builder deliveryRateChange(Double deliveryRateChange) { this.deliveryRateChange = deliveryRateChange; return this; }
        public Builder activeMembers(Integer activeMembers) { this.activeMembers = activeMembers; return this; }
        public Builder activeLoansAmount(Double activeLoansAmount) { this.activeLoansAmount = activeLoansAmount; return this; }
        public Builder lowStockAlerts(Integer lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }

        public CommunicationStatsResponse build() {
            return new CommunicationStatsResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}