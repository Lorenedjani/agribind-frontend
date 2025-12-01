package com.agribind.communication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "communication_stats")
public class CommunicationStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime statDate;

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

    @PrePersist
    protected void onCreate() {
        statDate = LocalDateTime.now();
    }

    // Static builder method
    public static CommunicationStatsBuilder builder() {
        return new CommunicationStatsBuilder();
    }

    // Builder class
    public static class CommunicationStatsBuilder {
        private Long id;
        private LocalDateTime statDate;
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

        public CommunicationStatsBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CommunicationStatsBuilder statDate(LocalDateTime statDate) {
            this.statDate = statDate;
            return this;
        }

        public CommunicationStatsBuilder totalMessagesSent(Integer totalMessagesSent) {
            this.totalMessagesSent = totalMessagesSent;
            return this;
        }

        public CommunicationStatsBuilder messagesSentThisWeek(Integer messagesSentThisWeek) {
            this.messagesSentThisWeek = messagesSentThisWeek;
            return this;
        }

        public CommunicationStatsBuilder audioMessagesTotal(Integer audioMessagesTotal) {
            this.audioMessagesTotal = audioMessagesTotal;
            return this;
        }

        public CommunicationStatsBuilder audioLanguagesSupported(Integer audioLanguagesSupported) {
            this.audioLanguagesSupported = audioLanguagesSupported;
            return this;
        }

        public CommunicationStatsBuilder activeAlerts(Integer activeAlerts) {
            this.activeAlerts = activeAlerts;
            return this;
        }

        public CommunicationStatsBuilder criticalAlerts(Integer criticalAlerts) {
            this.criticalAlerts = criticalAlerts;
            return this;
        }

        public CommunicationStatsBuilder deliveryRate(Double deliveryRate) {
            this.deliveryRate = deliveryRate;
            return this;
        }

        public CommunicationStatsBuilder deliveryRateChange(Double deliveryRateChange) {
            this.deliveryRateChange = deliveryRateChange;
            return this;
        }

        public CommunicationStatsBuilder activeMembers(Integer activeMembers) {
            this.activeMembers = activeMembers;
            return this;
        }

        public CommunicationStatsBuilder activeLoansAmount(Double activeLoansAmount) {
            this.activeLoansAmount = activeLoansAmount;
            return this;
        }

        public CommunicationStatsBuilder lowStockAlerts(Integer lowStockAlerts) {
            this.lowStockAlerts = lowStockAlerts;
            return this;
        }

        public CommunicationStats build() {
            CommunicationStats stats = new CommunicationStats();
            stats.setId(this.id);
            stats.setStatDate(this.statDate);
            stats.setTotalMessagesSent(this.totalMessagesSent);
            stats.setMessagesSentThisWeek(this.messagesSentThisWeek);
            stats.setAudioMessagesTotal(this.audioMessagesTotal);
            stats.setAudioLanguagesSupported(this.audioLanguagesSupported);
            stats.setActiveAlerts(this.activeAlerts);
            stats.setCriticalAlerts(this.criticalAlerts);
            stats.setDeliveryRate(this.deliveryRate);
            stats.setDeliveryRateChange(this.deliveryRateChange);
            stats.setActiveMembers(this.activeMembers);
            stats.setActiveLoansAmount(this.activeLoansAmount);
            stats.setLowStockAlerts(this.lowStockAlerts);
            return stats;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStatDate() { return statDate; }
    public void setStatDate(LocalDateTime statDate) { this.statDate = statDate; }

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
}