package com.agribind.communication.controller;

import com.agribind.communication.dto.*;
import com.agribind.communication.service.AudioMessageService;
import com.agribind.communication.service.CommunicationService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
@RequestMapping("/api/communications")
@CrossOrigin(origins = "*")
public class CommunicationController {

    @Autowired
    private DataSource dataSource;

    private static final Logger log = LoggerFactory.getLogger(CommunicationController.class);

    private final CommunicationService communicationService;
    private final AudioMessageService audioMessageService;

    public CommunicationController(CommunicationService communicationService, AudioMessageService audioMessageService) {
        this.communicationService = communicationService;
        this.audioMessageService = audioMessageService;
    }

    // ==================== SMS Endpoints ====================

    @PostMapping("/sms/bulk")
    public ResponseEntity<SmsMessageResponse> sendBulkSms(
        @Valid @RequestBody SmsMessageRequest request
    ) {
        log.info("Received bulk SMS request for: {}", request.getTargetAudience());
        SmsMessageResponse response = communicationService.sendBulkSms(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sms/compose")
    public ResponseEntity<BulkMessageResponse> composeBulkMessage(
        @Valid @RequestBody BulkMessageRequest request
    ) {
        log.info("Composing bulk message for: {}", request.getRecipientAudience());

        // Replace builder with direct object creation
        SmsMessageRequest smsRequest = new SmsMessageRequest();
        smsRequest.setTargetAudience(request.getRecipientAudience());
        smsRequest.setSpecificZone(request.getSpecificZone());
        smsRequest.setContent(request.getMessageContent());
        smsRequest.setPriority(request.getPriority());
        smsRequest.setScheduledAt(request.getScheduledDelivery());
        smsRequest.setTemplateId(request.getTemplateId());

        SmsMessageResponse smsResponse = communicationService.sendBulkSms(smsRequest);

        // Replace builder with direct object creation
        BulkMessageResponse response = new BulkMessageResponse();
        response.setMessageId(smsResponse.getId());
        response.setRecipientCount(smsResponse.getRecipientCount());
        response.setEstimatedCost(smsResponse.getEstimatedCost());
        response.setStatus(smsResponse.getStatus());
        response.setMessage("Message queued successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== Audio Message Endpoints ====================

    @PostMapping(value = "/audio/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AudioMessageResponse> uploadAudioMessage(
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam("language") String language,
        @RequestParam("targetAudience") String targetAudience,
        @RequestParam(value = "specificZone", required = false) String specificZone,
        @RequestParam(value = "autoPlay", defaultValue = "false") Boolean autoPlay,
        @RequestParam(value = "priority", defaultValue = "NORMAL") String priority
    ) {
        log.info("Uploading audio message: {} in {}", title, language);

        // Replace builder with direct object creation
        AudioMessageRequest request = new AudioMessageRequest();
        request.setTargetAudience(parseTargetAudience(targetAudience));
        request.setSpecificZone(specificZone);
        request.setTitle(title);
        request.setLanguage(language);
        request.setAutoPlay(autoPlay);
        request.setPriority(parseMessagePriority(priority));

        AudioMessageResponse response = communicationService.createAudioMessage(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/audio/broadcast")
    public ResponseEntity<AudioMessageResponse> broadcastAudioMessage(
        @Valid @RequestBody AudioMessageRequest request,
        @RequestParam("audioFileId") Long audioFileId
    ) {
        log.info("Broadcasting audio message: {}", request.getTitle());
        // Implementation for broadcasting existing audio
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audio/languages")
    public ResponseEntity<List<String>> getSupportedLanguages() {
        return ResponseEntity.ok(audioMessageService.getSupportedLanguages());
    }

    // ==================== Alert Endpoints ====================

    @PostMapping("/alerts")
    public ResponseEntity<AlertResponse> createAlert(
        @Valid @RequestBody AlertRequest request
    ) {
        log.info("Creating alert: {}", request.getTitle());
        AlertResponse response = communicationService.createAndSendAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/alerts/history")
    public ResponseEntity<List<AlertResponse>> getAlertHistory() {
        List<AlertResponse> alerts = communicationService.getAlertHistory();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable String alertId) {
        // Implementation for getting specific alert
        return ResponseEntity.ok().build();
    }

    // ==================== Resource Request Endpoints ====================

    @PostMapping("/resources/requests")
    public ResponseEntity<ResourceRequestResponse> createResourceRequest(
        @Valid @RequestBody ResourceRequestDto request
    ) {
        log.info("Creating resource request for: {}", request.getResourceName());
        ResourceRequestResponse response = communicationService.createResourceRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/resources/requests")
    public ResponseEntity<List<ResourceRequestResponse>> getActiveResourceRequests() {
        List<ResourceRequestResponse> requests = communicationService.getActiveResourceRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/resources/requests/{requestId}")
    public ResponseEntity<ResourceRequestResponse> getResourceRequestById(
        @PathVariable String requestId
    ) {
        // Implementation for getting specific resource request
        return ResponseEntity.ok().build();
    }

    @PutMapping("/resources/requests/{requestId}/match")
    public ResponseEntity<Void> matchSuppliers(
        @PathVariable String requestId,
        @RequestBody List<Long> supplierIds
    ) {
        log.info("Matching suppliers for request: {}", requestId);
        // Implementation for matching suppliers
        return ResponseEntity.ok().build();
    }

    // ==================== Template Endpoints ====================

    @PostMapping("/templates")
    public ResponseEntity<MessageTemplateDto> createTemplate(
        @Valid @RequestBody MessageTemplateDto template
    ) {
        log.info("Creating message template: {}", template.getName());
        MessageTemplateDto response = communicationService.createTemplate(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<MessageTemplateDto>> getAllTemplates() {
        List<MessageTemplateDto> templates = communicationService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<MessageTemplateDto> getTemplateById(@PathVariable Long id) {
        // Implementation for getting specific template
        return ResponseEntity.ok().build();
    }

    // ==================== Statistics Endpoints ====================

    @GetMapping("/statistics")
    public ResponseEntity<CommunicationStatsResponse> getStatistics() {
        CommunicationStatsResponse stats = communicationService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/statistics/refresh")
    public ResponseEntity<Void> refreshStatistics() {
        log.info("Refreshing communication statistics");
        communicationService.updateStatistics();
        return ResponseEntity.ok().build();
    }

    // ==================== Health Check ====================

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Communication Service is running");
    }

    @GetMapping("/health/db")
    public ResponseEntity<Map<String, Object>> databaseHealthCheck() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Test database connection
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5); // 5 second timeout
                health.put("database", "connected");
                health.put("valid", isValid);

                // Get database metadata
                DatabaseMetaData metaData = connection.getMetaData();
                health.put("databaseProductName", metaData.getDatabaseProductName());
                health.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                health.put("driverName", metaData.getDriverName());
                health.put("driverVersion", metaData.getDriverVersion());

                // Check if tables exist
                health.put("migrationStatus", "complete");

                return ResponseEntity.ok(health);
            }
        } catch (SQLException e) {
            health.put("database", "disconnected");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    @GetMapping("/health/migrations")
    public ResponseEntity<Map<String, Object>> migrationStatus() {
        Map<String, Object> status = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            // Check if flyway_schema_history table exists
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "flyway_schema_history", null);

            if (tables.next()) {
                // Table exists, get migration info
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT version, description, success FROM flyway_schema_history " +
                    "ORDER BY installed_rank DESC LIMIT 5"
                );

                List<Map<String, Object>> migrations = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> migration = new HashMap<>();
                    migration.put("version", rs.getString("version"));
                    migration.put("description", rs.getString("description"));
                    migration.put("success", rs.getBoolean("success"));
                    migrations.add(migration);
                }

                status.put("migrations", migrations);
                status.put("totalMigrations", migrations.size());
            } else {
                status.put("migrations", "flyway_schema_history table not found");
            }

            return ResponseEntity.ok(status);
        } catch (SQLException e) {
            status.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(status);
        }
    }

    // ==================== Helper Methods ====================

    private com.agribind.communication.model.TargetAudience parseTargetAudience(String audience) {
        try {
            return com.agribind.communication.model.TargetAudience.valueOf(audience.toUpperCase());
        } catch (IllegalArgumentException e) {
            return com.agribind.communication.model.TargetAudience.ALL_MEMBERS;
        }
    }

    private com.agribind.communication.model.MessagePriority parseMessagePriority(String priority) {
        try {
            return com.agribind.communication.model.MessagePriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return com.agribind.communication.model.MessagePriority.NORMAL;
        }
    }
}