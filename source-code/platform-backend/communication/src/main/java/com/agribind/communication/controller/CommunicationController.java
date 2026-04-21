package com.agribind.communication.controller;

import com.agribind.communication.dto.*;
import com.agribind.communication.service.AudioMessageService;
import com.agribind.communication.service.CommunicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/communications")
@CrossOrigin(origins = "*")
@Tag(name = "Communication Service", description = "API for managing SMS, audio messages, alerts, templates, and resource requests")
public class CommunicationController {

    private static final Logger log = LoggerFactory.getLogger(CommunicationController.class);

    private final CommunicationService communicationService;
    private final AudioMessageService audioMessageService;

    public CommunicationController(CommunicationService communicationService, AudioMessageService audioMessageService) {
        this.communicationService = communicationService;
        this.audioMessageService = audioMessageService;
    }

    // ==================== SMS Endpoints ====================

    @Operation(summary = "Send bulk SMS", description = "Send SMS messages to multiple recipients based on target audience")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "SMS sent successfully",
            content = @Content(schema = @Schema(implementation = SmsMessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/sms/bulk")
    public ResponseEntity<SmsMessageResponse> sendBulkSms(
        @Parameter(description = "SMS request details", required = true)
        @Valid @RequestBody SmsMessageRequest request
    ) {
        log.info("Received bulk SMS request for: {}", request.getTargetAudience());
        SmsMessageResponse response = communicationService.sendBulkSms(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Compose bulk message", description = "Compose and send a bulk message to selected audience")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Message composed and queued successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/sms/compose")
    public ResponseEntity<BulkMessageResponse> composeBulkMessage(
        @Parameter(description = "Bulk message request", required = true)
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

    // ==================== Broadcast Endpoints ====================

    @GetMapping("/broadcasts")
    public ResponseEntity<List<BroadcastDto>> getBroadcasts() {
        return ResponseEntity.ok(communicationService.getBroadcasts());
    }

    @PostMapping("/broadcasts")
    public ResponseEntity<BroadcastDto> createBroadcast(
        @Valid @RequestBody BroadcastRequestDto request
    ) {
        BroadcastDto created = communicationService.createBroadcast(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/broadcasts/{broadcastId}")
    public ResponseEntity<BroadcastDto> renameBroadcast(
        @PathVariable Long broadcastId,
        @RequestBody Map<String, String> body
    ) {
        String title = body.get("title");
        BroadcastDto updated = communicationService.renameBroadcast(broadcastId, title);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/broadcasts/{broadcastId}")
    public ResponseEntity<Void> deleteBroadcast(@PathVariable Long broadcastId) {
        boolean deleted = communicationService.deleteBroadcast(broadcastId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/audio/languages")
    public ResponseEntity<List<String>> getSupportedLanguages() {
        return ResponseEntity.ok(audioMessageService.getSupportedLanguages());
    }

    // ==================== Audio Library Endpoints ====================

    @GetMapping("/audio/library")
    public ResponseEntity<List<AudioLibraryItemDto>> getAudioLibrary() {
        return ResponseEntity.ok(communicationService.getAudioLibraryItems());
    }

    @PostMapping(value = "/audio/library", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AudioLibraryItemDto> uploadAudioLibraryItem(
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam("language") String language,
        @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds,
        @RequestParam(value = "durationLabel", required = false) String durationLabel
    ) {
        AudioLibraryItemDto created = communicationService.createAudioLibraryItem(file, title, language, durationSeconds, durationLabel);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/audio/library/{id}")
    public ResponseEntity<AudioLibraryItemDto> renameAudioLibraryItem(
        @PathVariable Long id,
        @RequestBody Map<String, String> body
    ) {
        String title = body.get("title");
        AudioLibraryItemDto updated = communicationService.renameAudioLibraryItem(id, title);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/audio/library/{id}")
    public ResponseEntity<Void> deleteAudioLibraryItem(@PathVariable Long id) {
        boolean deleted = communicationService.deleteAudioLibraryItem(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Serves an audio file directly from the filesystem by its unique filename.
     * Supports various audio formats (.mp3, .wav, .m4a, .webm).
     */
    @Operation(summary = "Serve audio file by filename")
    @GetMapping("/audio/{filename:.+}")
    public ResponseEntity<Resource> serveAudioFile(@PathVariable String filename) {
        try {
            log.info("Serving audio file: {}", filename);
            Path filePath = audioMessageService.getAudioFilePath("/audio/" + filename);
            Resource resource = new FileSystemResource(filePath.toFile());

            if (!resource.exists()) {
                log.warn("Audio file not found on disk: {}", filename);
                return ResponseEntity.notFound().build();
            }

            String lower = filename.toLowerCase();
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            if (lower.endsWith(".mp3")) {
                contentType = MediaType.parseMediaType("audio/mpeg");
            } else if (lower.endsWith(".wav")) {
                contentType = MediaType.parseMediaType("audio/wav");
            } else if (lower.endsWith(".m4a") || lower.endsWith(".aac")) {
                contentType = MediaType.parseMediaType("audio/mp4");
            } else if (lower.endsWith(".webm")) {
                contentType = MediaType.parseMediaType("audio/webm");
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header("Cache-Control", "public, max-age=86400")
                    .body(resource);

        } catch (Exception e) {
            log.error("Internal error serving audio file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        AlertResponse alert = communicationService.getAlertById(alertId);
        return alert != null ? ResponseEntity.ok(alert) : ResponseEntity.notFound().build();
    }

    @PutMapping("/alerts/{alertId}")
    public ResponseEntity<AlertResponse> updateAlert(
        @PathVariable String alertId,
        @Valid @RequestBody AlertRequest request
    ) {
        AlertResponse updated = communicationService.updateAlert(alertId, request);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/alerts/{alertId}")
    public ResponseEntity<Void> deleteAlert(@PathVariable String alertId) {
        boolean deleted = communicationService.deleteAlert(alertId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/alerts/{alertId}/send")
    public ResponseEntity<AlertResponse> markAlertSent(@PathVariable String alertId) {
        AlertResponse updated = communicationService.markAlertSent(alertId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
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
        boolean matched = communicationService.matchSuppliers(requestId, supplierIds);
        return matched ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
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
        MessageTemplateDto template = communicationService.getTemplateById(id);
        return template != null ? ResponseEntity.ok(template) : ResponseEntity.notFound().build();
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<MessageTemplateDto> updateTemplate(@PathVariable Long id, @Valid @RequestBody MessageTemplateDto template) {
        log.info("Updating message template: {}", id);
        MessageTemplateDto updatedTemplate = communicationService.updateTemplate(id, template);
        return updatedTemplate != null ? ResponseEntity.ok(updatedTemplate) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        log.info("Deleting message template: {}", id);
        boolean deleted = communicationService.deleteTemplate(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
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

    // ==================== Mobile + Offline Sync Endpoints ====================

    @GetMapping("/mobile/announcements")
    public ResponseEntity<List<MobileAnnouncementDto>> mobileAnnouncements(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime updatedSince
    ) {
        List<BroadcastDto> broadcasts = communicationService.getBroadcasts();
        List<MobileAnnouncementDto> items = new ArrayList<>();
        for (BroadcastDto broadcast : broadcasts) {
            if (updatedSince != null && broadcast.getDate() != null && !broadcast.getDate().isAfter(updatedSince)) {
                continue;
            }
            if (broadcast.getVariants() == null || broadcast.getVariants().isEmpty()) {
                continue;
            }
            for (BroadcastVariantDto variant : broadcast.getVariants()) {
                MobileAnnouncementDto dto = new MobileAnnouncementDto();
                String lang = variant.getLanguage() != null ? variant.getLanguage() : "";
                String safeLang = lang.replaceAll("[^a-zA-Z0-9_.-]", "_");
                dto.setId(broadcast.getId() + "-" + safeLang);
                dto.setTitle(broadcast.getTitle());
                dto.setLanguage(variant.getLanguage());
                dto.setFilePath(variant.getFilePath());
                String q = "broadcastId=" + broadcast.getId()
                        + "&language=" + URLEncoder.encode(lang, StandardCharsets.UTF_8);
                dto.setAudioUrl("/api/communications/mobile/broadcast-audio?" + q);
                dto.setDurationSeconds(variant.getDurationSeconds());
                dto.setAutoPlay(broadcast.isAutoPlay());
                dto.setUpdatedAt(broadcast.getDate());
                dto.setVersion(broadcast.getDate() != null ? broadcast.getDate().toEpochSecond(java.time.ZoneOffset.UTC) : 1L);
                dto.setChecksum(Integer.toHexString((broadcast.getId() + ":" + variant.getFilePath()).hashCode()));
                items.add(dto);
            }
        }
        return ResponseEntity.ok(items);
    }

    /**
     * Stream broadcast variant audio for mobile download / playback (gateway: same host as API).
     */
    @GetMapping("/mobile/broadcast-audio")
    public ResponseEntity<Resource> streamBroadcastAudio(
            @RequestParam Long broadcastId,
            @RequestParam String language
    ) {
        try {
            log.info("Streaming broadcast audio - broadcastId: {}, language: {}", broadcastId, language);

            List<BroadcastDto> broadcasts = communicationService.getBroadcasts();
            BroadcastDto found = broadcasts.stream()
                    .filter(b -> broadcastId.equals(b.getId()))
                    .findFirst()
                    .orElse(null);

            if (found == null || found.getVariants() == null) {
                log.warn("Broadcast not found or has no variants: {}", broadcastId);
                return ResponseEntity.notFound().build();
            }

            BroadcastVariantDto variant = found.getVariants().stream()
                    .filter(v -> v.getLanguage() != null && v.getLanguage().equalsIgnoreCase(language))
                    .findFirst()
                    .orElse(null);

            if (variant == null || variant.getFilePath() == null || variant.getFilePath().isBlank()) {
                log.warn("Variant not found for language: {} in broadcast: {}", language, broadcastId);
                return ResponseEntity.notFound().build();
            }

            Path localPath = audioMessageService.getAudioFilePath(variant.getFilePath());
            log.info("Looking for audio file at: {}", localPath);

            Resource resource = new FileSystemResource(localPath.toFile());
            if (!resource.exists()) {
                log.warn("Audio file not found on disk: {}", localPath);
                return ResponseEntity.notFound().build();
            }

            String lower = variant.getFilePath().toLowerCase();
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            if (lower.endsWith(".mp3")) {
                contentType = MediaType.parseMediaType("audio/mpeg");
            } else if (lower.endsWith(".wav")) {
                contentType = MediaType.parseMediaType("audio/wav");
            } else if (lower.endsWith(".m4a") || lower.endsWith(".aac")) {
                contentType = MediaType.parseMediaType("audio/mp4");
            } else if (lower.endsWith(".webm")) {
                contentType = MediaType.parseMediaType("audio/webm");
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header("Cache-Control", "public, max-age=86400")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error streaming broadcast audio for broadcastId: " + broadcastId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/audio/download/{audioLibraryId}")
    public ResponseEntity<Resource> downloadAudio(@PathVariable Long audioLibraryId) {
        try {
            log.info("Downloading audio with ID: {}", audioLibraryId);

            AudioLibraryItemDto item = communicationService.getAudioLibraryItems()
                    .stream()
                    .filter(i -> i.getId().equals(audioLibraryId))
                    .findFirst()
                    .orElse(null);

            if (item == null) {
                log.warn("Audio item not found with ID: {}", audioLibraryId);
                return ResponseEntity.notFound().build();
            }

            Path localPath = audioMessageService.getAudioFilePath(item.getFilePath());
            log.info("Looking for audio file at: {}", localPath);

            Resource resource = new FileSystemResource(localPath.toFile());
            if (!resource.exists()) {
                log.warn("Audio file not found on disk: {}", localPath);
                return ResponseEntity.notFound().build();
            }

            // Determine content type based on file extension
            String fileName = item.getFileName();
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

            if (fileName != null) {
                String lower = fileName.toLowerCase();
                if (lower.endsWith(".mp3")) {
                    contentType = MediaType.parseMediaType("audio/mpeg");
                } else if (lower.endsWith(".wav")) {
                    contentType = MediaType.parseMediaType("audio/wav");
                } else if (lower.endsWith(".m4a") || lower.endsWith(".aac")) {
                    contentType = MediaType.parseMediaType("audio/mp4");
                } else if (lower.endsWith(".webm")) {
                    contentType = MediaType.parseMediaType("audio/webm");
                }
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header("Cache-Control", "public, max-age=86400")
                    .header("ETag", Integer.toHexString((item.getId() + ":" + item.getFilePath()).hashCode()))
                    .header("Content-Disposition", "inline; filename=\"" + item.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading audio with ID: " + audioLibraryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncOutbox(@RequestBody SyncRequestDto request) {
        // Demonstrate deterministic 409 flow for financial operations.
        if ("FINANCIAL_TRANSACTION".equalsIgnoreCase(request.getActionType())) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", "conflict");
            body.put("clientOutboxId", request.getClientOutboxId());
            body.put("serverVersion", request.getPayload());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("status", "synced");
        body.put("clientOutboxId", request.getClientOutboxId());
        body.put("updatedEntities", communicationService.getBroadcasts());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/sync/conflict-resolution")
    public ResponseEntity<Map<String, Object>> resolveSyncConflict(@RequestBody SyncConflictResolutionDto request) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "resolved");
        body.put("clientOutboxId", request.getClientOutboxId());
        body.put("resolutionMethod", request.getResolutionMethod());
        body.put("resolvedPayload", "SERVER_WINS".equalsIgnoreCase(request.getResolutionMethod())
                ? request.getServerVersion()
                : request.getLocalVersion());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/sync/updates")
    public ResponseEntity<List<MobileAnnouncementDto>> syncUpdates(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since
    ) {
        return mobileAnnouncements(since);
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