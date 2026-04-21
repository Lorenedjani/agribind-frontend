package com.agribind.communication.dto;

import java.util.Map;

public class SyncConflictResolutionDto {
    private String clientOutboxId;
    private String actionType;
    private Map<String, Object> localVersion;
    private Map<String, Object> serverVersion;
    private String resolutionMethod;

    public String getClientOutboxId() { return clientOutboxId; }
    public void setClientOutboxId(String clientOutboxId) { this.clientOutboxId = clientOutboxId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public Map<String, Object> getLocalVersion() { return localVersion; }
    public void setLocalVersion(Map<String, Object> localVersion) { this.localVersion = localVersion; }
    public Map<String, Object> getServerVersion() { return serverVersion; }
    public void setServerVersion(Map<String, Object> serverVersion) { this.serverVersion = serverVersion; }
    public String getResolutionMethod() { return resolutionMethod; }
    public void setResolutionMethod(String resolutionMethod) { this.resolutionMethod = resolutionMethod; }
}
