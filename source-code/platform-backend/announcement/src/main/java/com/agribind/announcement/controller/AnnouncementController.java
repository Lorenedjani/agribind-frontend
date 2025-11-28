package com.agribind.announcement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.agribind.announcement.service.AnnouncementService;
import com.agribind.announcement.dto.AnnouncementDTO;
import com.agribind.announcement.dto.AudioMessageDTO;
import com.agribind.announcement.dto.CreateAnnouncementRequest;
import com.agribind.announcement.model.AudioMessage;
import com.agribind.announcement.model.Language;
import com.agribind.announcement.repository.AudioMessageRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private AudioMessageRepository audioMessageRepository;

    @PostMapping
    public ResponseEntity<AnnouncementDTO> createAnnouncement(
            @RequestBody CreateAnnouncementRequest request) {
        AnnouncementDTO announcement = announcementService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(announcement);
    }

    @PostMapping("/{id}/audio")
    public ResponseEntity<AudioMessageDTO> uploadAudio(
            @PathVariable Long id,
            @RequestParam("language") String language,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedBy) {
        try {
            AudioMessageDTO audioMessage = announcementService.uploadAudioMessage(
                id, language, file, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(audioMessage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementDTO>> getAllActiveAnnouncements() {
        List<AnnouncementDTO> announcements = announcementService.getActiveAnnouncements();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/cooperative/{cooperativeId}")
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncementsByCooperative(
            @PathVariable Long cooperativeId) {
        List<AnnouncementDTO> announcements =
            announcementService.getAnnouncementsByCooperative(cooperativeId);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementDTO> getAnnouncementById(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(announcement);
    }

    @GetMapping("/{id}/audio-messages")
    public ResponseEntity<List<AudioMessageDTO>> getAudioMessages(@PathVariable Long id) {
        List<AudioMessageDTO> audioMessages =
            announcementService.getAudioMessagesForAnnouncement(id);
        return ResponseEntity.ok(audioMessages);
    }

    @GetMapping("/{id}/audio/{language}")
    public ResponseEntity<Resource> streamAudio(
            @PathVariable Long id,
            @PathVariable String language) {
        try {
            AudioMessage audioMessage = audioMessageRepository
                .findByAnnouncementIdAndLanguage(id, Language.valueOf(language.toUpperCase()));

            if (audioMessage == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(audioMessage.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + audioMessage.getFileName() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/audio/{language}/play")
    public ResponseEntity<Void> trackAudioPlay(
            @PathVariable Long id,
            @PathVariable String language) {
        announcementService.trackAudioPlay(id, language);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<Void> publishAnnouncement(@PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return ResponseEntity.ok().build();
    }
}