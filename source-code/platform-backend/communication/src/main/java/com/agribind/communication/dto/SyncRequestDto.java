package com.agribind.communication.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class SyncRequestDto {
    private String clientOutboxId;
    private String actionType;
    private Map<String, Object> payload;
    private String deviceId;
    private LocalDateTime timestamp;

    public String getClientOutboxId() { return clientOutboxId; }
    public void setClientOutboxId(String clientOutboxId) { this.clientOutboxId = clientOutboxId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
