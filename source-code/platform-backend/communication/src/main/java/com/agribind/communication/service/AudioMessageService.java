package com.agribind.communication.service;


import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AudioMessageService {


    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${communication.audio.max-duration-seconds}")
    private Integer maxDurationSeconds;

    @Value("${communication.audio.supported-formats}")
    private String supportedFormats;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * Upload and store audio file
     */
    public String uploadAudioFile(MultipartFile file, String language) throws IOException {
        // Validate file
        validateAudioFile(file);

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = String.format("%s_%s_%s.%s",
            UUID.randomUUID().toString(),
            language,
            System.currentTimeMillis(),
            extension
        );

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Audio file uploaded successfully: {}", uniqueFilename);

        return "/audio/" + uniqueFilename;
    }

    /**
     * Validate audio file
     */
    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Audio file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum limit of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);

        List<String> supportedFormatsList = Arrays.asList(supportedFormats.split(","));
        if (!supportedFormatsList.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                String.format("Unsupported file format. Supported formats: %s", supportedFormats)
            );
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Delete audio file
     */
    public boolean deleteAudioFile(String fileUrl) {
        try {
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Audio file deleted: {}", filename);
                return true;
            }

            return false;
        } catch (IOException e) {
            log.error("Error deleting audio file: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get supported languages for audio messages
     */
    public List<String> getSupportedLanguages() {
        return Arrays.asList(
            "French",
            "English",
            "Fulfulde",
            "Ewondo",
            "Duala"
        );
    }

    /**
     * Validate language
     */
    public boolean isValidLanguage(String language) {
        return getSupportedLanguages().stream()
            .anyMatch(lang -> lang.equalsIgnoreCase(language));
    }

    /**
     * Get audio file path from a URL or filename.
     * Extracts only the filename component to resolve against the upload directory.
     */
    public Path getAudioFilePath(String fileUrl) {
        if (fileUrl == null) return null;

        String filename = fileUrl;
        if (fileUrl.startsWith("/audio/")) {
            filename = fileUrl.substring(7);
        } else if (fileUrl.contains("/")) {
            filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }
        return Paths.get(uploadDir).resolve(filename);
    }
}