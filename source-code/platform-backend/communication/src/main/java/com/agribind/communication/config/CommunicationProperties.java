package com.agribind.communication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "communication")
@Data
public class CommunicationProperties {
    private SmsProperties sms;
    private AudioProperties audio;
    private Map<String, List<String>> zones;

    @Data
    public static class SmsProperties {
        private Double costPerMessage;
    }

    @Data
    public static class AudioProperties {
        private Integer maxDurationSeconds;
        private String supportedFormats;
    }
}
