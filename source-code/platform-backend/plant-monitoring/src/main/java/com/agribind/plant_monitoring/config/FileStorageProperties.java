package com.agribind.plant_monitoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileStorageProperties {

    @Value("${file.upload.dir}")
    private String uploadDir;
    private long maxSize;
    private String[] allowedExtensions;
    private Thumbnail thumbnail = new Thumbnail();
    
    // Getters and Setters
    public static class Thumbnail {
        private int width = 300;
        private int height = 300;
        
        // Getters and Setters
        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(String[] allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

}