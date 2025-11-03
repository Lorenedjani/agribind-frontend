package cm.agribind.usermanagement.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
public class FileStorageUtil {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public String storeProfilePicture(String userId, byte[] imageData, String contentType) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "profiles");
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String extension = getFileExtension(contentType);
            String filename = String.format("%s_%s.%s", userId, UUID.randomUUID(), extension);
            Path filePath = uploadPath.resolve(filename);

            // Write file
            Files.write(filePath, imageData);

            log.info("Profile picture stored: {}", filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("Failed to store profile picture for user: {}", userId, e);
            throw new RuntimeException("Failed to store profile picture", e);
        }
    }

    public String storeProfilePicture(String userId, MultipartFile file) {
        try {
            return storeProfilePicture(userId, file.getBytes(), file.getContentType());
        } catch (IOException e) {
            log.error("Failed to read multipart file for user: {}", userId, e);
            throw new RuntimeException("Failed to store profile picture", e);
        }
    }

    public byte[] loadProfilePicture(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
            return null;
        } catch (IOException e) {
            log.error("Failed to load profile picture: {}", filePath, e);
            return null;
        }
    }

    public boolean deleteProfilePicture(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete profile picture: {}", filePath, e);
            return false;
        }
    }

    public String getProfilePictureUrl(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        // In production, this would return a CDN URL or serve via a controller
        return "/api/uploads/profiles/" + Paths.get(filePath).getFileName().toString();
    }

    private String getFileExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "bin";
        };
    }

    public void initializeStorage() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Files.createDirectories(uploadPath.resolve("profiles"));
            Files.createDirectories(uploadPath.resolve("exports"));
            Files.createDirectories(uploadPath.resolve("qrcodes"));

            log.info("File storage initialized at: {}", uploadPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to initialize file storage", e);
        }
    }
}