package com.agribind.plant_monitoring.service;

import com.agribind.plant_monitoring.config.FileStorageProperties;
import com.agribind.plant_monitoring.exception.FileStorageException;
import lombok.Getter;
import lombok.Setter;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final FileStorageProperties fileStorageProperties;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) throws IOException {
        this.fileStorageProperties = fileStorageProperties;
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        Files.createDirectories(this.fileStorageLocation);
    }

    public String storeFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        if (!isValidExtension(fileExtension)) {
            throw new FileStorageException("Invalid file extension: " + fileExtension);
        }

        if (file.getSize() > fileStorageProperties.getMaxSize()) {
            throw new FileStorageException("File size exceeds maximum limit");
        }

        String filename = generateUniqueFilename(fileExtension);
        Path targetLocation = this.fileStorageLocation.resolve(filename);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public String createThumbnail(String filename) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(filename);

        String thumbnailFilename = "thumb_" + filename;
        Path thumbnailPath = this.fileStorageLocation.resolve(thumbnailFilename);

        // Thumbnailator replaces Scalr — same quality/fit behaviour, no extra dependency needed
        Thumbnails.of(filePath.toFile())
                .size(
                        fileStorageProperties.getThumbnail().getWidth(),
                        fileStorageProperties.getThumbnail().getHeight()
                )
                .keepAspectRatio(true)
                .toFile(thumbnailPath.toFile());

        return thumbnailFilename;
    }

    public void deleteFile(String filename) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(filename);
        Files.deleteIfExists(filePath);

        // Also delete thumbnail if exists
        Path thumbnailPath = this.fileStorageLocation.resolve("thumb_" + filename);
        Files.deleteIfExists(thumbnailPath);
    }

    public byte[] getFileAsBytes(String filename) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(filename);
        return Files.readAllBytes(filePath);
    }

    public Path loadFile(String filename) {
        return fileStorageLocation.resolve(filename).normalize();
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID() + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private boolean isValidExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }

        for (String allowedExt : fileStorageProperties.getAllowedExtensions()) {
            if (extension.equals("." + allowedExt.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public ImageMetadata extractImageMetadata(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

        ImageMetadata metadata = new ImageMetadata();
        metadata.setWidth(image.getWidth());
        metadata.setHeight(image.getHeight());
        metadata.setSize(file.getSize());
        metadata.setContentType(file.getContentType());

        return metadata;
    }

    @Getter
    @Setter
    public static class ImageMetadata {
        private Integer width;
        private Integer height;
        private Long size;
        private String contentType;
    }
}