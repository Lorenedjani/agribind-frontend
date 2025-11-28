package com.agribind.announcement.service;

import com.agribind.announcement.repository.AnnouncementRepository;
import com.agribind.announcement.repository.AudioMessageRepository;
import com.agribind.announcement.dto.AnnouncementDTO;
import com.agribind.announcement.dto.AudioMessageDTO;
import com.agribind.announcement.dto.CreateAnnouncementRequest;
import com.agribind.announcement.model.Announcement;
import com.agribind.announcement.model.AudioMessage;
import com.agribind.announcement.model.AnnouncementType;
import com.agribind.announcement.model.Priority;
import com.agribind.announcement.model.Language;
import com.agribind.announcement.model.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AudioMessageRepository audioMessageRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Transactional
    public AnnouncementDTO createAnnouncement(CreateAnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setDescription(request.getDescription());
        announcement.setType(AnnouncementType.valueOf(request.getType()));
        announcement.setPriority(Priority.valueOf(request.getPriority()));
        announcement.setCooperativeId(request.getCooperativeId());
        announcement.setTargetAudience(request.getTargetAudience());
        announcement.setPublishDate(request.getPublishDate());
        announcement.setExpirationDate(request.getExpirationDate());

        announcement = announcementRepository.save(announcement);
        return convertToDTO(announcement);
    }

    @Transactional
    public AudioMessageDTO uploadAudioMessage(Long announcementId, String language,
                                             MultipartFile file, String uploadedBy) throws IOException {
        Announcement announcement = announcementRepository.findById(announcementId)
            .orElseThrow(() -> new RuntimeException("Announcement not found"));

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, String.valueOf(announcementId));
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + "_" + language + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create audio message record
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setAnnouncement(announcement);
        audioMessage.setLanguage(Language.valueOf(language.toUpperCase()));
        audioMessage.setFileName(uniqueFilename);
        audioMessage.setFilePath(filePath.toString());
        audioMessage.setFileUrl("/api/announcements/" + announcementId + "/audio/" + language);
        audioMessage.setFileSize(file.getSize());
        audioMessage.setUploadedBy(uploadedBy);

        audioMessage = audioMessageRepository.save(audioMessage);
        return convertToDTO(audioMessage);
    }

    public List<AnnouncementDTO> getActiveAnnouncements() {
        List<Announcement> announcements = announcementRepository
            .findActiveAnnouncements(LocalDateTime.now());
        return announcements.stream()
            .map(announcement -> convertToDTO(announcement))
            .collect(Collectors.toList());
    }

    public List<AnnouncementDTO> getAnnouncementsByCooperative(Long cooperativeId) {
        List<Announcement> announcements = announcementRepository
            .findActiveAnnouncementsByCooperative(cooperativeId, LocalDateTime.now());
        return announcements.stream()
            .map(announcement -> convertToDTO(announcement))
            .collect(Collectors.toList());
    }

    public AnnouncementDTO getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Announcement not found"));
        announcement.incrementViewCount();
        announcementRepository.save(announcement);
        return convertToDTO(announcement);
    }

    public List<AudioMessageDTO> getAudioMessagesForAnnouncement(Long announcementId) {
        List<AudioMessage> audioMessages = audioMessageRepository.findByAnnouncementId(announcementId);
        return audioMessages.stream()
            .map(announcement -> convertToDTO(announcement))
            .collect(Collectors.toList());
    }

    @Transactional
    public void trackAudioPlay(Long announcementId, String language) {
        AudioMessage audioMessage = audioMessageRepository
            .findByAnnouncementIdAndLanguage(announcementId, Language.valueOf(language.toUpperCase()));
        if (audioMessage != null) {
            audioMessage.incrementPlayCount();
            audioMessageRepository.save(audioMessage);
        }
    }

    @Transactional
    public void publishAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Announcement not found"));
        announcement.setStatus(Status.PUBLISHED);
        announcement.setPublishDate(LocalDateTime.now());
        announcementRepository.save(announcement);
    }

    private AnnouncementDTO convertToDTO(Announcement announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setDescription(announcement.getDescription());
        dto.setType(announcement.getType().name());
        dto.setPriority(announcement.getPriority().name());
        dto.setCooperativeId(announcement.getCooperativeId());
        dto.setTargetAudience(announcement.getTargetAudience());
        dto.setPublishDate(announcement.getPublishDate());
        dto.setExpirationDate(announcement.getExpirationDate());
        dto.setStatus(announcement.getStatus().name());
        dto.setViewCount(announcement.getViewCount());
        dto.setDownloadCount(announcement.getDownloadCount());

        List<AudioMessageDTO> audioMessages = announcement.getAudioMessages().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        dto.setAudioMessages(audioMessages);

        return dto;
    }

    private AudioMessageDTO convertToDTO(AudioMessage audioMessage) {
        AudioMessageDTO dto = new AudioMessageDTO();
        dto.setId(audioMessage.getId());
        dto.setLanguage(audioMessage.getLanguage().name());
        dto.setLanguageDisplayName(audioMessage.getLanguage().getDisplayName());
        dto.setFileName(audioMessage.getFileName());
        dto.setFileUrl(audioMessage.getFileUrl());
        dto.setFileSize(audioMessage.getFileSize());
        dto.setDuration(audioMessage.getDuration());
        dto.setUploadedBy(audioMessage.getUploadedBy());
        dto.setUploadedAt(audioMessage.getUploadedAt());
        dto.setPlayCount(audioMessage.getPlayCount());
        return dto;
    }
}
