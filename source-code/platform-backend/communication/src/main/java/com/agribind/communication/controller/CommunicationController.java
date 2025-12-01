package com.agribind.communication.controller;

import com.agribind.communication.dto.*;
import com.agribind.communication.service.AudioMessageService;
import com.agribind.communication.service.CommunicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/communications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CommunicationController {

    private final CommunicationService communicationService;
    private final AudioMessageService audioMessageService;

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

        SmsMessageRequest smsRequest = SmsMessageRequest.builder()
            .targetAudience(request.getRecipientAudience())
            .specificZone(request.getSpecificZone())
            .content(request.getMessageContent())
            .priority(request.getPriority())
            .scheduledAt(request.getScheduledDelivery())
            .templateId(request.getTemplateId())
            .build();

        SmsMessageResponse smsResponse = communicationService.sendBulkSms(smsRequest);

        BulkMessageResponse response = BulkMessageResponse.builder()
            .messageId(smsResponse.getId())
            .recipientCount(smsResponse.getRecipientCount())
            .estimatedCost(smsResponse.getEstimatedCost())
            .status(smsResponse.getStatus())
            .message("Message queued successfully")
            .build();

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

        AudioMessageRequest request = AudioMessageRequest.builder()
            .targetAudience(parseTargetAudience(targetAudience))
            .specificZone(specificZone)
            .title(title)
            .language(language)
            .autoPlay(autoPlay)
            .priority(parseMessagePriority(priority))
            .build();

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