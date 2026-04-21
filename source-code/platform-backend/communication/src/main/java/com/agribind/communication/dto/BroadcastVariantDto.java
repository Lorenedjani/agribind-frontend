package com.agribind.communication.dto;

public class BroadcastVariantDto {
    private Long audioLibraryId;
    private String language;
    private String filePath;
    private String fileName;
    private Integer durationSeconds;
    private String durationLabel;
    private Integer listenCount;
    private boolean reused;

    public Long getAudioLibraryId() { return audioLibraryId; }
    public void setAudioLibraryId(Long audioLibraryId) { this.audioLibraryId = audioLibraryId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getDurationLabel() { return durationLabel; }
    public void setDurationLabel(String durationLabel) { this.durationLabel = durationLabel; }

    public Integer getListenCount() { return listenCount; }
    public void setListenCount(Integer listenCount) { this.listenCount = listenCount; }

    public boolean isReused() { return reused; }
    public void setReused(boolean reused) { this.reused = reused; }
}

