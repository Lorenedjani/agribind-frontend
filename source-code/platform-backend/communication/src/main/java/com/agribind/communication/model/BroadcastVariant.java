package com.agribind.communication.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "broadcast_variants")
public class BroadcastVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broadcast_id")
    private Broadcast broadcast;

    private String language;

    private Long audioLibraryId;
    private String filePath;
    private String fileName;

    private Integer durationSeconds;
    private String durationLabel;

    private Integer listenCount;

    @PrePersist
    protected void onCreate() {
        if (listenCount == null) listenCount = 0;
        if (durationSeconds == null) durationSeconds = 0;
        if (durationLabel == null) durationLabel = "0:00";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Broadcast getBroadcast() { return broadcast; }
    public void setBroadcast(Broadcast broadcast) { this.broadcast = broadcast; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Long getAudioLibraryId() { return audioLibraryId; }
    public void setAudioLibraryId(Long audioLibraryId) { this.audioLibraryId = audioLibraryId; }

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
}

