package com.agribind.communication.util;

import com.agribind.communication.exception.InvalidFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for file operations
 */
@Component
public class FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    // Allowed audio file extensions
    private static final List<String> ALLOWED_AUDIO_EXTENSIONS = Arrays.asList(
            "mp3", "wav", "ogg", "m4a", "aac"
    );

    // Allowed audio MIME types
    private static final List<String> ALLOWED_AUDIO_MIME_TYPES = Arrays.asList(
            "audio/mpeg",        // mp3
            "audio/wav",         // wav
            "audio/x-wav",       // wav
            "audio/ogg",         // ogg
            "audio/mp4",         // m4a
            "audio/x-m4a",       // m4a
            "audio/aac"          // aac
    );

    // Maximum file size (50MB in bytes)
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    /**
     * Validate audio file
     *
     * @param file File to validate
     * @throws InvalidFileException if file is invalid
     */
    public void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or null");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !hasValidAudioExtension(filename)) {
            throw new InvalidFileException(
                    "Invalid file extension. Allowed: " + String.join(", ", ALLOWED_AUDIO_EXTENSIONS)
            );
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid file type. Allowed audio formats: MP3, WAV, OGG, M4A, AAC"
            );
        }

        log.info("Audio file validation successful: {}", filename);
    }

    /**
     * Check if filename has valid audio extension
     *
     * @param filename Filename to check
     * @return true if valid, false otherwise
     */
    private boolean hasValidAudioExtension(String filename) {
        String extension = getFileExtension(filename);
        return extension != null && ALLOWED_AUDIO_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Get file extension from filename
     *
     * @param filename Filename
     * @return File extension or null
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }

        return null;
    }

    /**
     * Generate unique filename
     *
     * @param originalFilename Original filename
     * @param language Language code (optional)
     * @return Unique filename
     */
    public String generateUniqueFilename(String originalFilename, String language) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        if (language != null && !language.isEmpty()) {
            return String.format("%s_%s_%d.%s", uuid, language, timestamp, extension);
        }

        return String.format("%s_%d.%s", uuid, timestamp, extension);
    }

    /**
     * Save file to directory
     *
     * @param file File to save
     * @param uploadDir Upload directory path
     * @param filename Filename to use
     * @return Path to saved file
     * @throws IOException if save fails
     */
    public Path saveFile(MultipartFile file, String uploadDir, String filename) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(filename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved successfully: {}", filePath);
        }

        return filePath;
    }

    /**
     * Delete file
     *
     * @param filePath Path to file
     * @return true if deleted, false otherwise
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted successfully: {}", filePath);
                return true;
            } else {
                log.warn("File does not exist: {}", filePath);
                return false;
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Check if file exists
     *
     * @param filePath Path to file
     * @return true if exists, false otherwise
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Get file size in bytes
     *
     * @param filePath Path to file
     * @return File size or -1 if error
     */
    public long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Error getting file size: {}", filePath, e);
            return -1;
        }
    }

    /**
     * Format file size for display
     *
     * @param bytes Size in bytes
     * @return Formatted size string
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";

        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Sanitize filename by removing special characters
     *
     * @param filename Filename to sanitize
     * @return Sanitized filename
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        // Remove path separators and special characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Get MIME type from file extension
     *
     * @param filename Filename
     * @return MIME type or null
     */
    public String getMimeTypeFromExtension(String filename) {
        String extension = getFileExtension(filename);
        if (extension == null) {
            return null;
        }

        switch (extension.toLowerCase()) {
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";
            case "m4a":
                return "audio/mp4";
            case "aac":
                return "audio/aac";
            default:
                return null;
        }
    }
}