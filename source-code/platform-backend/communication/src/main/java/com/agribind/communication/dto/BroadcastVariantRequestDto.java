package com.agribind.communication.dto;

import jakarta.validation.constraints.NotNull;

public class BroadcastVariantRequestDto {
    @NotNull
    private Long audioLibraryId;

    @NotNull
    private String language;

    private String filePath;  // Add this field

    public Long getAudioLibraryId() {
        return audioLibraryId;
    }

    public void setAudioLibraryId(Long audioLibraryId) {
        this.audioLibraryId = audioLibraryId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}