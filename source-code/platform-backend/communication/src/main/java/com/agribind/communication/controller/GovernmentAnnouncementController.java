package com.agribind.communication.controller;

import com.agribind.communication.dto.*;
import com.agribind.communication.service.AudioMessageService;
import com.agribind.communication.service.CommunicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.agribind.communication.model.AlertType;
import com.agribind.communication.model.AlertPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/communications/government")
@CrossOrigin(origins = "*")
@Tag(name = "Government Portal", description = "Government agent endpoints for farmer announcements")
public class GovernmentAnnouncementController {

    private static final Logger log = LoggerFactory.getLogger(GovernmentAnnouncementController.class);

    private final CommunicationService communicationService;
    private final AudioMessageService  audioMessageService;

    public GovernmentAnnouncementController(
            CommunicationService communicationService,
            AudioMessageService  audioMessageService) {
        this.communicationService = communicationService;
        this.audioMessageService  = audioMessageService;
    }

    @Operation(summary = "Publish a government audio announcement")
    @PostMapping(value = "/announcements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('GOVERNMENT','ADMIN')")
    public ResponseEntity<Map<String, Object>> publishAnnouncement(
            @RequestParam("file")                                                 MultipartFile file,
            @RequestParam("title")                                                String title,
            @RequestParam("language")                                             String language,
            @RequestParam(value = "targetAudience", defaultValue = "ALL_MEMBERS") String targetAudience,
            @RequestParam(value = "specificZone",   required = false)             String specificZone,
            @RequestParam(value = "priority",       defaultValue = "NORMAL")      String priority,
            @RequestParam(value = "source",         defaultValue = "Government")  String source
    ) {
        log.info("Government announcement – title='{}' lang='{}' audience='{}'",
                title, language, targetAudience);
        try {
            if (!audioMessageService.isValidLanguage(language)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Unsupported language: " + language));
            }

            // Store audio file
            String filePath = audioMessageService.uploadAudioFile(file, language);

            // Build variant using the REAL DTO (BroadcastVariantRequestDto)
            BroadcastVariantRequestDto variant = new BroadcastVariantRequestDto();
            variant.setLanguage(language);
            // Note: BroadcastVariantRequestDto doesn't have setFilePath, only setAudioLibraryId
            // We'll need to handle this differently - perhaps we need to save to audio library first
            // For now, let's create an audio library item and use its ID
            AudioLibraryItemDto audioItem = communicationService.createAudioLibraryItem(
                    file, title, language, null, null);
            variant.setAudioLibraryId(audioItem.getId());

            // Build broadcast request using the REAL DTO (BroadcastRequestDto)
            BroadcastRequestDto request = new BroadcastRequestDto();
            request.setTitle("[GOV] " + title);
            request.setType("AUDIO");
            request.setTargetAudience(targetAudience);
            // BroadcastRequestDto doesn't have setSpecificZone - it's handled in variant
            request.setAutoPlay(false);
            request.setSmsTranscription(false);
            request.setEmailEnabled(false);
            request.setScheduleFor(LocalDateTime.now());
            request.setVariants(List.of(variant));

            BroadcastDto saved = communicationService.createBroadcast(request);

            String audioUrl = "/api/communications/mobile/broadcast-audio"
                    + "?broadcastId=" + saved.getId()
                    + "&language=" + URLEncoder.encode(language, StandardCharsets.UTF_8);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id",             saved.getId(),
                    "title",          title,
                    "language",       language,
                    "source",         source,
                    "targetAudience", targetAudience,
                    "audioUrl",       audioUrl,
                    "publishedAt",    LocalDateTime.now().toString(),
                    "status",         "PUBLISHED"
            ));

        } catch (Exception e) {
            log.error("Failed to publish government announcement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "List government announcements")
    @GetMapping("/announcements")
    @PreAuthorize("hasAnyRole('GOVERNMENT','ADMIN')")
    public ResponseEntity<List<BroadcastDto>> listAnnouncements() {
        List<BroadcastDto> gov = communicationService.getBroadcasts().stream()
                .filter(b -> b.getTitle() != null && b.getTitle().startsWith("[GOV]"))
                .toList();
        return ResponseEntity.ok(gov);
    }

    @Operation(summary = "Delete a government announcement")
    @DeleteMapping("/announcements/{id}")
    @PreAuthorize("hasAnyRole('GOVERNMENT','ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        boolean deleted = communicationService.deleteBroadcast(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Publish a government emergency alert")
    @PostMapping("/alerts")
    @PreAuthorize("hasAnyRole('GOVERNMENT','ADMIN')")
    public ResponseEntity<Map<String, Object>> publishAlert(
            @Valid @RequestBody GovernmentAlertRequest body
    ) {
        log.info("Government alert – title='{}' type='{}' priority='{}'",
                body.getTitle(), body.getAlertType(), body.getPriority());

        AlertRequest alertRequest = new AlertRequest();
        alertRequest.setTitle("[GOV] " + body.getTitle());
        // Fix: Convert String to AlertType enum
        alertRequest.setType(AlertType.valueOf(body.getAlertType().toUpperCase()));
        // Fix: Convert String to AlertPriority enum
        alertRequest.setPriority(AlertPriority.valueOf(body.getPriority().toUpperCase()));
        // Fix: Convert String to TargetAudience enum
        alertRequest.setTargetAudience(
                com.agribind.communication.model.TargetAudience.valueOf(
                        body.getTargetAudience().toUpperCase()));
        alertRequest.setContent(body.getContent());
        alertRequest.setChannels(
                body.getChannels() != null ? body.getChannels() : List.of("SMS", "PUSH"));

        // Fix: CommunicationService has createAndSendAlert, not createAlert
        AlertResponse created = communicationService.createAndSendAlert(alertRequest);
        communicationService.markAlertSent(created.getAlertId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "alertId", created.getAlertId(),
                "title",   body.getTitle(),
                "status",  "SENT",
                "sentAt",  LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> supportedLanguages() {
        return ResponseEntity.ok(audioMessageService.getSupportedLanguages());
    }
}