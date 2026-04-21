package com.agribind.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SmsService {

    @Value("${local-gateway.url:http://192.168.x.x:8082/}")
    private String gatewayUrl;

    @Value("${local-gateway.token:}")
    private String gatewayToken;

    private final RestTemplate restTemplate;

    public SmsService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendSms(String toPhoneNumber, String messageBody) {
        // Dev Mode Mock
        if (gatewayUrl.contains("192.168.x.x") || gatewayUrl.isBlank()) {
            log.warn("🚧 [DEV MODE] Traccar Gateway URL is not configured. (Current: {})", gatewayUrl);
            log.warn("📲 Would have commanded phone to send to {}: {}", formatPhoneNumber(toPhoneNumber), messageBody);
            return;
        }

        try {
            sendViaTraccar(formatPhoneNumber(toPhoneNumber), messageBody);
            log.info("✅ SMS dispatch command successfully sent to local Traccar Gateway on your phone!");
            
        } catch (Exception e) {
            log.error("❌ Failed to reach Traccar Gateway on your phone at URL: {}", gatewayUrl, e);
            throw new RuntimeException("Local Phone SMS sending failed", e);
        }
    }

    private void sendViaTraccar(String to, String messageBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Traccar supports an authorization token if configured in the app
        if (gatewayToken != null && !gatewayToken.isEmpty()) {
            headers.set("Authorization", gatewayToken);
        }

        // Traccar payload schema
        Map<String, String> payload = new HashMap<>();
        payload.put("to", to);
        payload.put("message", messageBody);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        
        restTemplate.postForEntity(gatewayUrl, request, String.class);
    }

    private String formatPhoneNumber(String phone) {
        if (!phone.startsWith("+")) {
            return "+237" + phone.replaceAll("[^0-9]", "");
        }
        return phone;
    }
}