package com.agribind.communication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "communication")
public class CommunicationProperties {
    private SmsProperties sms;
    private AudioProperties audio;
    private Map<String, List<String>> zones;

    public SmsProperties getSms() {
        return sms;
    }

    public void setSms(SmsProperties sms) {
        this.sms = sms;
    }

    public AudioProperties getAudio() {
        return audio;
    }

    public void setAudio(AudioProperties audio) {
        this.audio = audio;
    }

    public Map<String, List<String>> getZones() {
        return zones;
    }

    public void setZones(Map<String, List<String>> zones) {
        this.zones = zones;
    }

    public static class SmsProperties {
        private Double costPerMessage;

        public Double getCostPerMessage() {
            return costPerMessage;
        }

        public void setCostPerMessage(Double costPerMessage) {
            this.costPerMessage = costPerMessage;
        }
    }

    public static class AudioProperties {
        private Integer maxDurationSeconds;
        private String supportedFormats;

        public Integer getMaxDurationSeconds() {
            return maxDurationSeconds;
        }

        public void setMaxDurationSeconds(Integer maxDurationSeconds) {
            this.maxDurationSeconds = maxDurationSeconds;
        }

        public String getSupportedFormats() {
            return supportedFormats;
        }

        public void setSupportedFormats(String supportedFormats) {
            this.supportedFormats = supportedFormats;
        }
    }
}